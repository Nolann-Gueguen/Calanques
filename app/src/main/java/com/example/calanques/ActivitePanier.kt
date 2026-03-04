package com.example.calanques

data class ActivitePanier(
    val titre: String,
    val date: String,
    val personnes: Int,
    val prixParPersonne: Int
) {
    val montant: Int
        get() = personnes * prixParPersonne
}
