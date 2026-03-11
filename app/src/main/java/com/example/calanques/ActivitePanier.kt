package com.example.calanques


data class ReservationActivite(
    val activity_id: Int,
    val date: String,
    val heure: String,
    val titre_activite: String? = "Activité",
    val prix_unitaire: Int = 0,
    val nb_participants: Int
) {
    val montant: Int get() = nb_participants * prix_unitaire
}


data class ReservationResponse(
    val id: Int,
    val date: String,
    val commentaire: String?,
    val status_reservation_id: Int,
    val activities: List<ReservationActivite>
)
// Envoie une ligne d'activité au panier
data class ReservationActiviteCreate(
    val activity_id: Int,
    val date: String,
    val heure: String,
    val nb_participants: Int
)

// Création de réservation (POST)
data class ReservationCreate(
    val date: String,
    val commentaire: String? = null,
    val status_reservation_id: Int = 1,
    val activities: List<ReservationActiviteCreate>
)