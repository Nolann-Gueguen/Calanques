package com.example.calanques

// Le format exact du colis à envoyer pour la création de compte
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

// La réponse que le serveur te renverra
data class ApiResponse(
    val message: String? = null
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)

// Le profil exact renvoyé par /api/users/me
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

// Le moule pour envoyer les modifications au serveur
data class UserUpdateRequest(
    val nom: String,
    val prenom: String,
    val email: String,
    val adresse: String,
    val cp: String,
    val ville: String,
    val telephone: String
)


data class ReservationActivity(
    val activite_id: Int,
    val date_activite: String,
    val heure_activite: String,
    val nb_participants: Int
)