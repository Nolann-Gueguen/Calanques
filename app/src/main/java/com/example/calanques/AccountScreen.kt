package com.example.calanques

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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

// --- OUTILS DE FORMATAGE DES DATES ---
val sqlDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
val frenchDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
val sqlTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
val frenchTimeFormat = SimpleDateFormat("HH'h'mm", Locale.getDefault())

// --- TRADUCTEUR DE STATUTS ---
fun getStatusInfo(statusId: Int): Pair<String, Color> {
    return when (statusId) {
        1 -> Pair("Confirmée", Color(0xFF4CAF50))
        2 -> Pair("Annulée", Color(0xFFF44336))
        else -> Pair("Inconnu", Color.Gray)
    }
}

@Composable
fun AccountScreen(onReservationClick: (ReservationResponse) -> Unit) {
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
                onSaveSuccess = { isEditing = false; currentUserProfile = null }
            )
        } else {
            ProfileScreen(
                token = userToken!!,
                onReservationClick = onReservationClick,
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
    onReservationClick: (ReservationResponse) -> Unit,
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            // 1. Charger le profil
            if (userProfileState == null) {
                userProfile = RetrofitClient.instance.getMe("Bearer $token")
                onProfileLoaded(userProfile!!)
            } else {
                userProfile = userProfileState
            }

            // 2. Charger TOUTES les activités pour avoir les noms et tarifs (activitiesMap)
            val acts = RetrofitClient.instance.getActivites()
            activitiesMap = acts.associateBy { it.id }

            // 3. Charger les réservations
            reservations = RetrofitClient.instance.getMyReservations("Bearer $token")
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            Log.e("API_DEBUG", "Erreur chargement profil", e)
            errorMessage = "Erreur de synchronisation avec le serveur."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = CalanquesBlue, modifier = Modifier.padding(top = 50.dp))
        } else if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, fontWeight = FontWeight.Bold)
            Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) { Text("Se déconnecter") }
        } else {
            val user = userProfile!!

            Icon(Icons.Default.AccountCircle, null, Modifier.size(100.dp).padding(bottom = 16.dp), tint = CalanquesBlue)
            val fullName = "${user.prenom ?: ""} ${user.nom ?: ""}".trim()
            Text(fullName.ifEmpty { "Utilisateur" }, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CalanquesBlue)
            Text(user.email, fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // Carte informations profil
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CalanquesLightGrey)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Détails du compte", fontWeight = FontWeight.Bold, color = CalanquesBlue)
                    InfoRow(label = "Prénom", value = user.prenom)
                    InfoRow(label = "Nom", value = user.nom)
                    InfoRow(label = "Téléphone", value = user.telephone, icon = "📞")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Mes Réservations", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CalanquesBlue, modifier = Modifier.align(Alignment.Start))

            if (reservations.isEmpty()) {
                Text("Aucune réservation.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                reservations.forEach { res ->
                    ReservationCard(
                        res = res,
                        activitiesMap = activitiesMap,
                        onClick = {
                            // --- LOGIQUE DE RÉPARATION DES DONNÉES AU CLIC ---
                            scope.launch {
                                try {
                                    // On essaie l'API d'abord
                                    val apiDetails = RetrofitClient.instance.getReservationActivities("Bearer $token", res.id)

                                    // On fusionne avec activitiesMap pour être SUR d'avoir le tarif et le nom
                                    val repairedActivities = apiDetails.map { detail ->
                                        val infoRef = activitiesMap[detail.activity_id]
                                        detail.copy(
                                            titre_activite = infoRef?.nom ?: detail.titre_activite ?: "Activité",
                                            prix_unitaire = infoRef?.tarif ?: detail.prix_unitaire
                                        )
                                    }
                                    onReservationClick(res.copy(activities = repairedActivities))
                                } catch (e: Exception) {
                                    // Si l'API échoue, on répare manuellement ce qu'on a déjà
                                    val manualRepair = res.activities.map { detail ->
                                        val infoRef = activitiesMap[detail.activity_id]
                                        detail.copy(
                                            titre_activite = infoRef?.nom ?: "Activité",
                                            prix_unitaire = infoRef?.tarif ?: 0.0
                                        )
                                    }
                                    onReservationClick(res.copy(activities = manualRepair))
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(onClick = { onEditProfile(user) }, modifier = Modifier.fillMaxWidth()) {
                Text("Modifier mes informations", color = CalanquesBlue)
            }
            TextButton(onClick = onLogout) { Text("Se déconnecter", color = CalanquesRed) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationCard(res: ReservationResponse, activitiesMap: Map<Int, Activite>, onClick: () -> Unit) {
    val dateFr = try { frenchDateFormat.format(sqlDateFormat.parse(res.date)!!) } catch (e: Exception) { res.date }
    val (statusText, statusColor) = getStatusInfo(res.status_reservation_id)

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Réservation #${res.id}", fontWeight = FontWeight.Bold, color = CalanquesBlue)
                    Text("Le $dateFr", fontSize = 12.sp, color = Color.Gray)
                }
                Surface(color = statusColor.copy(0.15f), shape = RoundedCornerShape(12.dp)) {
                    Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            // Aperçu des activités dans la carte
            res.activities.forEach { detail ->
                val info = activitiesMap[detail.activity_id]
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(info?.nom ?: "Activité", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${detail.nb_participants} pers.", color = CalanquesBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?, icon: String? = null) {
    if (!value.isNullOrBlank()) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text("${icon ?: ""} $value".trim(), fontSize = 14.sp, color = Color.Black)
        }
    }
}