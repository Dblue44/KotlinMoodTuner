package com.example.kotlinmoodtuner.Interface

import com.example.kotlinmoodtuner.Retrofit.ResponseModel
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @Headers("Accept: application/json")
    @POST("uploadPhoto")
    fun uploadPhoto(
        @Part file: MultipartBody.Part
    ): Call<ResponseModel>
}