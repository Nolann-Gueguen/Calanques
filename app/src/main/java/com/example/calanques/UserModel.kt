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

    // L'API ne renvoie pas ces champs dans /api/reservations,
    // donc on les laisse en optionnel pour le mapping manuel
    val titre_activite: String? = null,
    val prix_unitaire: Double = 0.0,

    @SerializedName("nb_participants")
    val nb_participants: Int = 0
) {
    // Cette propriété calculée fonctionnera dès qu'on aura injecté le vrai prix
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
    val activite_id: Int,
    val date_activite: String,
    val heure_activite: String,
    val nb_participants: Int
)

data class ReservationCreate(
    val date: String,
    val activites: List<ReservationActiviteCreate>, // <-- On change "activities" par "activites"
    val commentaire: String = "", // <-- On ajoute le champ commentaire obligatoire
    val status_reservation_id: Int = 1 // (Garde ce que tu avais déjà ici)
)

data class StatusUpdateRequest(val statut_reservation_id: Int)

// ==========================================
// 4. MODÈLE TYPE D'ACTIVITÉ
// ==========================================

data class TypeActivite(
    val id: Int,
    @SerializedName("libelle") val libelle: String,
    @SerializedName("image_url") val image_url: String? = null
)