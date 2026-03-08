package com.example.calanques

import retrofit2.http.GET

interface ApiService {
    // Suppression du slash final pour correspondre exactement à l'endpoint de l'API
    @GET("api/activities")
    suspend fun getActivites(): List<Activite>
}