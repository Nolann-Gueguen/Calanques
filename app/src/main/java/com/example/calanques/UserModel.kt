package com.example.calanques

import com.google.gson.annotations.SerializedName

// ==========================================
// 1. MODÈLES UTILISATEUR (AUTHENTIFICATION & PROFIL)
// ==========================================

data class UserCreateRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val password: String,
    val adresse: String,
    val cp: String,
    val ville: String,
    val telephone: String,
    val role_id: Int = 1
)

data class ApiResponse(
    val message: String? = null
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)

data class UserResponse(
    val id: Int,
    val email: String,
    val nom: String?,
    val prenom: String?,
    val adresse: String?,
    val cp: String?,
    val ville: String?,
    val telephone: String?,
    val role_id: Int,
    val is_active: Boolean
)

data class UserUpdateRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val adresse: String,
    val cp: String,
    val ville: String,
    val telephone: String
)

// ==========================================
// 2. MODÈLES RÉSERVATION (DÉTAIL & AFFICHAGE)
// ==========================================

data class ReservationActivite(
    @SerializedName("activite_id")
    val activity_id: Int,

    @SerializedName("date_activite")
    val date: String?,

    @SerializedName("heure_activite")
    val heure: String?,

    @SerializedName("nom") // Indispensable pour récupérer le nom depuis le JSON
    val titre_activite: String? = "Activité",

    @SerializedName("image_url")
    val image_url: String? = null,

    @SerializedName("tarif") // Indispensable pour que le prix ne soit plus à 0
    val prix_unitaire: Double = 0.0,

    @SerializedName("nb_participants")
    val nb_participants: Int = 0
) {
    // Calcul automatique du prix total pour cette ligne
    val montant: Double get() = nb_participants * prix_unitaire
}

data class ReservationResponse(
    val id: Int,
    val date: String,
    val commentaire: String?,
    @SerializedName("statut_reservation_id")
    val status_reservation_id: Int,
    val activities: List<ReservationActivite>
)

// ==========================================
// 3. MODÈLES PANIER (CRÉATION DE RÉSERVATION)
// ==========================================

data class ReservationActiviteCreate(
    val activity_id: Int,
    val date: String,
    val heure: String,
    val nb_participants: Int
)

data class ReservationCreate(
    val date: String,
    val commentaire: String? = null,
    val status_reservation_id: Int = 1,
    val activities: List<ReservationActiviteCreate>
)