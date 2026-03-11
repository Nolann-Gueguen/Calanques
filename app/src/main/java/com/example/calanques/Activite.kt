package com.example.calanques

data class Activite(
    val id: Int,
    val nom: String,
    val description: String,
    val tarif: Double,
    val duree: String,
    val image_url: String,
    val type_id: Int
)