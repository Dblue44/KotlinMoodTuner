package com.example.kotlinmoodtuner

import android.Manifest
import android.R.attr.bitmap
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlinmoodtuner.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private var pickedPhoto : Uri? = null
    private var pickedBitMap : Bitmap? = null
    private var photoBytes : ByteArray? = null

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.uploadPhotoButton.setOnClickListener {
            pickPhoto()
        }


    }



    private fun pickPhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
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
                        val source = ImageDecoder.createSource(this.contentResolver, pickedPhoto!!)
                        pickedBitMap = ImageDecoder.decodeBitmap(source)

                        val file = File(cacheDir, "picked_image.jpeg")
                        file.createNewFile()

                        val bos = ByteArrayOutputStream()
                        pickedBitMap!!.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                        val bitmapData = bos.toByteArray()
                        photoBytes = bitmapData

                        val fos = FileOutputStream(file)
                        fos.write(photoBytes)
                        fos.close()

                        val musicIntent = Intent(this, MusicActivity::class.java)
                        musicIntent.putExtra("fileName", file.absolutePath);
                        startActivity(musicIntent)
                    }
                }
            }
        }
    )
}