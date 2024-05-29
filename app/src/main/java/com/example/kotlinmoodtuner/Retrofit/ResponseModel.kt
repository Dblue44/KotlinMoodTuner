package com.example.kotlinmoodtuner.Retrofit

import com.google.gson.annotations.SerializedName

data class Music(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("artist")
    val artist: String,

    @field:SerializedName("trackName")
    val trackName: String,

    @field:SerializedName("photoId")
    val photoId: String,
)

data class Prediction(
    @field:SerializedName("happy")
    val happy: Float,

    @field:SerializedName("sad")
    val sad: Float,

    @field:SerializedName("normal")
    val normal: Float,

    @field:SerializedName("angry")
    val angry: Float
)

data class ResponseModel(
    @field:SerializedName("music")
    val musicList: List<Music>,

    @field:SerializedName("prediction")
    val prediction: Prediction
)