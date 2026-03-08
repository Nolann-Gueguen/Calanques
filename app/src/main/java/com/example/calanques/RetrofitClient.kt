package com.example.calanques

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Utilisation de l'IP spéciale pour pointer vers l'ordinateur hôte (Wamp) [cite: 200]
    private const val BASE_URL = "http://10.0.2.2/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}