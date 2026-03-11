package com.example.calanques

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesRed
import com.example.calanques.ui.theme.CalanquesLightGrey
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// --- OUTILS DE FORMATAGE DES DATES (SQL -> Français) ---
val sqlDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
val frenchDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
val sqlTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
val frenchTimeFormat = SimpleDateFormat("HH'h'mm", Locale.getDefault())

// --- TRADUCTEUR DE STATUTS ---
// --- TRADUCTEUR DE STATUTS ---
fun getStatusInfo(statusId: Int): Pair<String, Color> {
    // Les couleurs standard adaptées à ton schema.sql
    return when (statusId) {
        1 -> Pair("Confirmée", Color(0xFF4CAF50))  // Vert
        2 -> Pair("Annulée", Color(0xFFF44336))    // Rouge
        else -> Pair("Inconnu", Color.Gray)        // Sécurité au cas où
    }
}
@Composable
fun AccountScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var userToken by remember { mutableStateOf(sessionManager.fetchAuthToken()) }
    var isSigningUp by remember { mutableStateOf(false) }

    var isEditing by remember { mutableStateOf(false) }
    var currentUserProfile by remember { mutableStateOf<UserResponse?>(null) }

    if (userToken != null) {
        if (isEditing && currentUserProfile != null) {
            EditProfileScreen(
                user = currentUserProfile!!,
                token = userToken!!,
                onBack = { isEditing = false },
                onSaveSuccess = {
                    isEditing = false
                    currentUserProfile = null
                }
            )
        } else {
            ProfileScreen(
                token = userToken!!,
                onLogout = {
                    sessionManager.clearAuthToken()
                    userToken = null
                    currentUserProfile = null
                },
                onEditProfile = { profile ->
                    currentUserProfile = profile
                    isEditing = true
                },
                onProfileLoaded = { profile ->
                    currentUserProfile = profile
                },
                userProfileState = currentUserProfile
            )
        }
    } else {
        if (isSigningUp) {
            SignUpScreen(
                onSignUpSuccess = { isSigningUp = false },
                onNavigateToLogin = { isSigningUp = false }
            )
        } else {
            LoginScreen(
                onLoginSuccess = { token ->
                    sessionManager.saveAuthToken(token)
                    userToken = token
                },
                onNavigateToSignUp = { isSigningUp = true }
            )
        }
    }
}

@Composable
fun ProfileScreen(
    token: String,
    onLogout: () -> Unit,
    onEditProfile: (UserResponse) -> Unit,
    onProfileLoaded: (UserResponse) -> Unit,
    userProfileState: UserResponse?
) {
    var userProfile by remember { mutableStateOf(userProfileState) }
    var reservations by remember { mutableStateOf<List<ReservationResponse>>(emptyList()) }
    var activitiesMap by remember { mutableStateOf<Map<Int, Activite>>(emptyMap()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        try {
            isLoading = true

            // 1. Profil
            if (userProfileState == null) {
                val response = RetrofitClient.instance.getMe("Bearer $token")
                userProfile = response
                onProfileLoaded(response)
            } else {
                userProfile = userProfileState
            }

            // 2. Dictionnaire des noms d'activités
            val acts = RetrofitClient.instance.getActivites()
            activitiesMap = acts.associateBy { it.id }

            // 3. Réservations
            reservations = RetrofitClient.instance.getMyReservations("Bearer $token")

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            Log.e("API_DEBUG", "Erreur chargement données profil", e)
            errorMessage = "Erreur de synchronisation avec le serveur."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = CalanquesBlue, modifier = Modifier.padding(top = 50.dp))
        } else if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, fontWeight = FontWeight.Bold)
            Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) { Text("Se déconnecter") }
        } else {
            val user = userProfile!!

            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.size(100.dp).padding(bottom = 16.dp),
                tint = CalanquesBlue
            )

            val fullName = "${user.prenom ?: ""} ${user.nom ?: ""}".trim()
            Text(text = fullName.ifEmpty { "Utilisateur" }, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CalanquesBlue)
            Text(text = user.email, fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // --- CARTE INFOS CONTACT ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CalanquesLightGrey),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Détails du compte", fontWeight = FontWeight.Bold, color = CalanquesBlue, modifier = Modifier.padding(bottom = 12.dp))
                    InfoRow(label = "Prénom", value = user.prenom)
                    InfoRow(label = "Nom", value = user.nom)
                    InfoRow(label = "Téléphone", value = user.telephone, icon = "📞")
                    val fullAddress = "${user.adresse ?: ""}\n${user.cp ?: ""} ${user.ville ?: ""}".trim()
                    if (fullAddress.length > 5) InfoRow(label = "Adresse", value = fullAddress, icon = "📍")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION RÉSERVATIONS ---
            Text(
                text = "Mes Réservations",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CalanquesBlue,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            if (reservations.isEmpty()) {
                Text("Aucune réservation enregistrée.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                reservations.forEach { res ->
                    ReservationCard(res, activitiesMap)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { onEditProfile(user) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CalanquesBlue))
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = CalanquesBlue, modifier = Modifier.padding(end = 8.dp))
                Text("Modifier mes informations", color = CalanquesBlue)
            }

            TextButton(onClick = onLogout) {
                Text("Se déconnecter", color = CalanquesRed)
            }
        }
    }
}

@Composable
fun ReservationCard(res: ReservationResponse, activitiesMap: Map<Int, Activite>) {
    // Formatage de la date de la commande globale
    val dateCommandeFr = try {
        val dateObj = sqlDateFormat.parse(res.date)
        frenchDateFormat.format(dateObj!!)
    } catch (e: Exception) { res.date }

    // Récupération des infos du statut
    val (statusText, statusColor) = getStatusInfo(res.status_reservation_id)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- EN-TÊTE AVEC BADGE STATUT ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Réservation #${res.id}", fontWeight = FontWeight.Bold, color = CalanquesBlue)
                    Text(text = "Le $dateCommandeFr", fontSize = 12.sp, color = Color.Gray)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f), // Fond léger
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor, // Texte vif
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            res.activities.forEach { detail ->
                val activiteInfo = activitiesMap[detail.activity_id]

                // Formatage de la date de l'activité
                val dateActFr = try {
                    val d = sqlDateFormat.parse(detail.date ?: "")
                    frenchDateFormat.format(d!!)
                } catch (e: Exception) { detail.date ?: "Date NC" }

                // Formatage de l'heure
                val heureFr = try {
                    val h = sqlTimeFormat.parse(detail.heure ?: "")
                    frenchTimeFormat.format(h!!)
                } catch (e: Exception) { detail.heure?.take(5) ?: "--:--" }

                Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activiteInfo?.nom ?: detail.titre_activite ?: "Activité",
                            fontWeight = FontWeight.Bold
                        )
                        // Affichage à la française
                        Text(text = "📅 $dateActFr à $heureFr", fontSize = 12.sp)
                    }
                    Text(
                        text = "${detail.nb_participants} pers.",
                        fontWeight = FontWeight.Bold,
                        color = CalanquesBlue
                    )
                }

                if (detail.montant > 0) {
                    Text(
                        text = "Sous-total : ${detail.montant}€",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?, icon: String? = null) {
    if (!value.isNullOrBlank()) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = "${icon ?: ""} $value".trim(), fontSize = 14.sp, color = Color.Black)
        }
    }
}