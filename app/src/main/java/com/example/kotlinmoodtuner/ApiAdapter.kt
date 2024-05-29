package com.example.kotlinmoodtuner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmoodtuner.Retrofit.Music
import com.squareup.picasso.Picasso
import com.example.kotlinmoodtuner.Retrofit.ResponseModel

class ApiAdapter(private val context: Context, private val responseData: ResponseModel): RecyclerView.Adapter<ApiAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.musicIco)
        val author: TextView = itemView.findViewById(R.id.artist)
        val musicName: TextView = itemView.findViewById(R.id.musicName)

        fun bind(listItem: Music) {
            image.setOnClickListener {
                Toast.makeText(it.context, "нажал на ${itemView.findViewById<View>(R.id.musicIco)}", Toast.LENGTH_SHORT)
                    .show()
            }
            itemView.setOnClickListener {
                Toast.makeText(it.context, "нажал на ${itemView.findViewById<View>(R.id.musicName)}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount() = responseData.musicList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val listItem = responseData.musicList[position]
        holder.bind(listItem)
        val url = "https://api-diploma-susu-24.ru/api/v1/react/music?musicId=" + responseData.musicList[position].photoId
        Picasso.get().load(url).into(holder.image)
        holder.author.text = responseData.musicList[position].artist
        holder.musicName.text = responseData.musicList[position].trackName
    }

}