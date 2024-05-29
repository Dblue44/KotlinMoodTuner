package com.example.kotlinmoodtuner

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinmoodtuner.Interface.ApiService
import com.example.kotlinmoodtuner.Retrofit.Common
import com.example.kotlinmoodtuner.Retrofit.ResponseModel
import com.example.kotlinmoodtuner.databinding.ActivityMusicBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class MusicActivity : AppCompatActivity() {

    private var pickedPhoto : Uri? = null
    private var pickedBitMap : Bitmap? = null
    private var photoBytes : ByteArray? = null
    private var pointsData: List<PieEntry> = listOf(PieEntry(0.8f, "Happy"),
        PieEntry(0.5f, "Sad"),
        PieEntry(0.2f, "Neutral"),
        PieEntry(0.1f, "Angry"))

    private lateinit var apiService: ApiService
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: ApiAdapter

    private lateinit var binding: ActivityMusicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityMusicBinding.inflate(layoutInflater)

        apiService = Common.retrofitService
        binding.musicList.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        binding.musicList.layoutManager = layoutManager

        val fileName = intent.getStringExtra("fileName")
        val fis = FileInputStream(fileName?.let { File(it) })
        pickedBitMap = BitmapFactory.decodeStream(fis);
        fis.close()

        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            pickPhoto()
        }

        uploadPhoto()

        val dataSet = PieDataSet(pointsData, "")
        dataSet.setColors(
            Color.rgb(68, 148, 74),
            Color.rgb(120, 162, 183),
            Color.rgb(107, 105, 224),
            Color.rgb(238, 32, 77))
        dataSet.setValueTextColor(Color.BLACK)
        dataSet.valueTextSize = 15f

        val pieData = PieData(dataSet)

        binding.pieChart.setData(pieData)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.animateXY(1000, 1000)
        binding.pieChart.setEntryLabelTextSize(15f)
        binding.pieChart.setEntryLabelColor(Color.BLACK)
    }

    private fun uploadPhoto() {
        pickedBitMap?.let { bitmap ->
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Ожидание ответа от сервера...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            val myDialogFragment = BlankFragmentDialog()
            val manager = supportFragmentManager
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()
            val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, byteArray.size)
            val body: MultipartBody.Part = MultipartBody.Part.createFormData("file", "photo.jpg", requestBody)
            apiService.uploadPhoto(body).enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    if (response.code() == 422) {
                        return
                    }
                    if (response.code() == 200) {
                        adapter = ApiAdapter(baseContext, response.body()!! as ResponseModel)
                        binding.musicList.adapter = adapter
                        val data = response.body()!!
                        val happy = data.prediction.happy
                        val sad = data.prediction.sad
                        val angry = data.prediction.angry
                        val normal = data.prediction.normal
                        pointsData = listOf(PieEntry(happy, "Happy"),
                            PieEntry(sad, "Sad"),
                            PieEntry(normal, "Neutral"),
                            PieEntry(angry, "Angry"))
                        val dataSet = PieDataSet(pointsData, "")
                        dataSet.setColors(
                            Color.rgb(68, 148, 74),
                            Color.rgb(120, 162, 183),
                            Color.rgb(107, 105, 224),
                            Color.rgb(238, 32, 77))
                        dataSet.setValueTextColor(Color.BLACK)
                        dataSet.valueTextSize = 15f
                        val pieData = PieData(dataSet)
                        binding.pieChart.setData(pieData)
                        binding.pieChart.notifyDataSetChanged()
                        binding.pieChart.invalidate()
                        progressDialog.dismiss()
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    Log.d("Error", t.toString())
                    progressDialog.dismiss()
                    val transaction: FragmentTransaction = manager.beginTransaction()
                    myDialogFragment.show(transaction, "dialog")
                }
            })
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "picked_image.jpg")
        file.createNewFile()

        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData = bos.toByteArray()
        photoBytes = bitmapData

        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()

        return file
    }

    private fun pickPhoto() {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            newActivityResultLauncher.launch(intent)
        }
    }

    private val newActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if (it.resultCode == RESULT_OK) {
                val intent = it.data
                if (intent != null) {
                    pickedPhoto = intent.data
                    if (pickedPhoto != null) {
                        val source = ImageDecoder.createSource(this.contentResolver,pickedPhoto!!)
                        pickedBitMap = ImageDecoder.decodeBitmap(source)
                        uploadPhoto()
                    }
                }
            }
        }
    )
}