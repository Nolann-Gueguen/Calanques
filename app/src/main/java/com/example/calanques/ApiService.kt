package com.example.calanques

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    // Suppression du slash final pour correspondre exactement à l'endpoint de l'API
    @GET("api/activities")
    suspend fun getActivites(): List<Activite>

    //Ajout de l'endpoint API pour Reservation
    @GET("api/reservations")
    suspend fun getReservations(): List<ReservationResponse>

    @DELETE("api/reservations/{reservation_id}")
    suspend fun deleteReservation(@Path("reservation_id") id: Int): Response<Unit>
}