package com.example.calanques
import com.google.gson.annotations.SerializedName


data class ReservationActivite(
    @SerializedName("activite_id") // Doit correspondre au nom dans PHPMyAdmin
    val activity_id: Int,

    @SerializedName("date_activite") // Doit correspondre au nom dans PHPMyAdmin
    val date: String?,

    @SerializedName("heure_activite") // Doit correspondre au nom dans PHPMyAdmin
    val heure: String?,

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
    @SerializedName("statut_reservation_id") // Vérifie si c'est 'statut' ou 'status' en SQL
    val status_reservation_id: Int,
    val activities: List<ReservationActivite>
)