package com.example.calanques

import retrofit2.http.GET

interface ApiService {
    @GET("api/activities/")
    suspend fun getActivites(): List<Activite>
}