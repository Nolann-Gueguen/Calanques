package com.example.calanques

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @POST("api/auth/signup")
    suspend fun registerUser(@Body request: UserCreateRequest): ApiResponse

    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String, // FastAPI utilise 'username' pour recevoir l'e-mail
        @Field("password") password: String
    ): TokenResponse

    // La route pour récupérer le profil avec le pass VIP
    @GET("api/users/me")
    suspend fun getMe(@Header("Authorization") token: String): UserResponse

    @PUT("api/users/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body request: UserUpdateRequest
    ): UserResponse
}