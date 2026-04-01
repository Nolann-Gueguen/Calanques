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

    // --- ACTIVITÉS ---
    @GET("api/activities/")
    suspend fun getActivites(): List<Activite>

    @GET("api/activities/{id}")
    suspend fun getActivityDetail(@Path("id") id: Int): Activite

    // --- TYPES D'ACTIVITÉS ---
    @GET("api/activity-types/")
    suspend fun getTypesActivites(): List<TypeActivite>

    // --- RÉSERVATIONS ---
    @GET("api/reservations/my")
    suspend fun getMyReservations(@Header("Authorization") token: String): List<ReservationResponse>

    @GET("api/reservations/")
    suspend fun getReservations(@Header("Authorization") token: String): List<ReservationResponse>

    @POST("api/reservations/")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body reservation: ReservationCreate
    ): Response<ReservationResponse>

    @DELETE("api/reservations/{reservation_id}/activities/{act_id}")
    suspend fun deleteActivityFromReservation(
        @Header("Authorization") token: String,
        @Path("reservation_id") reservationId: Int,
        @Path("act_id") activityId: Int
    ): Response<Unit>

    @PUT("api/reservations/{id}/status")
    suspend fun updateReservationStatus(
        @Header("Authorization") token: String,
        @Path("id") reservationId: Int,
        @Body statusRequest: StatusUpdateRequest
    ): ApiResponse

    @GET("api/reservations/{reservation_id}/activities")
    suspend fun getReservationActivities(
        @Header("Authorization") token: String,
        @Path("reservation_id") reservationId: Int
    ): List<ReservationActivite>

    // --- AUTH ---
    @POST("api/auth/signup")
    suspend fun registerUser(@Body request: UserCreateRequest): ApiResponse

    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    // --- UTILISATEURS ---
    @GET("api/users/me")
    suspend fun getMe(@Header("Authorization") token: String): UserResponse

    @PUT("api/users/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body request: UserUpdateRequest
    ): UserResponse
}