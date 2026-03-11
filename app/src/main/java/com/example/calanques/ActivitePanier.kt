package com.example.calanques

data class ActivitePanier(
    val reservation_id: Int,
    val activite_id: Int,
    val titre: String, // Jointure
    val date_activite: String,
    val heure_activite: String,
    val nb_participants: Int,
    val prixParPersonne: Int // Jointure
) {
    val montant: Int
        get() = nb_participants * prixParPersonne
}