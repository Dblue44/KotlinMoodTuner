package com.example.kotlinmoodtuner.Retrofit

import com.example.kotlinmoodtuner.Interface.ApiService

object Common {
    private val BASE_URL = "https://api-diploma-susu-24.ru/api/v1/react/"
    val retrofitService: ApiService
        get() = RetrofitClient.getClient(BASE_URL).create(ApiService::class.java)
}