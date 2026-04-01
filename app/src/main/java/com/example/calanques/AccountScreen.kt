package com.example.calanques

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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

// --- OUTILS DE FORMATAGE ---
val sqlDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
val frenchDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

fun getStatusInfo(statusId: Int): Pair<String, Color> {
    return when (statusId) {
        1 -> Pair("Confirmée", Color(0xFF4CAF50))
        2 -> Pair("Annulée", Color(0xFFF44336))
        else -> Pair("Inconnu", Color.Gray)
    }
}

@Composable
fun AccountScreen(
    onReservationClick: (ReservationResponse) -> Unit,
    onLogout: () -> Unit // AJOUTÉ : Reçu depuis MainActivity via HomeScreen
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userToken = sessionManager.fetchAuthToken()

    var isEditing by remember { mutableStateOf(false) }
    var currentUserProfile by remember { mutableStateOf<UserResponse?>(null) }

    // Si on n'a pas de token (sécurité), on appelle le logout pour retourner au login
    if (userToken == null) {
        LaunchedEffect(Unit) { onLogout() }
        return
    }

    if (isEditing && currentUserProfile != null) {
        EditProfileScreen(
            user = currentUserProfile!!,
            token = userToken,
            onBack = { isEditing = false },
            onSaveSuccess = { isEditing = false; currentUserProfile = null }
        )
    } else {
        ProfileScreen(
            token = userToken,
            onReservationClick = onReservationClick,
            onLogout = onLogout, // Transmis ici
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
            // 1. Charger le profil (Le token contient déjà "Bearer")
            if (userProfileState == null) {
                userProfile = RetrofitClient.instance.getMe(token)
                onProfileLoaded(userProfile!!)
            } else {
                userProfile = userProfileState
            }
            // 2. Charger les activités
            activitiesMap = RetrofitClient.instance.getActivites().associateBy { it.id }
            // 3. Charger les réservations
            reservations = RetrofitClient.instance.getMyReservations(token)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Erreur de synchronisation."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = CalanquesBlue)
        } else if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
            Button(onClick = onLogout) { Text("Se déconnecter") }
        } else {
            userProfile?.let { user ->
                Icon(Icons.Default.AccountCircle, null, Modifier.size(100.dp), tint = CalanquesBlue)
                Text("${user.prenom} ${user.nom}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(user.email, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CalanquesLightGrey)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(label = "Téléphone", value = user.telephone)
                        InfoRow(label = "Adresse", value = "${user.adresse}, ${user.cp} ${user.ville}")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Mes Réservations", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

                reservations.forEach { res ->
                    ReservationCard(res = res, activitiesMap = activitiesMap, onClick = {
                        scope.launch {
                            val details = RetrofitClient.instance.getReservationActivities(token, res.id)
                            onReservationClick(res.copy(activities = details))
                        }
                    })
                }

                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(onClick = { onEditProfile(user) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Modifier le profil")
                }
                TextButton(onClick = onLogout) {
                    Text("Se déconnecter", color = CalanquesRed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationCard(res: ReservationResponse, activitiesMap: Map<Int, Activite>, onClick: () -> Unit) {
    val (statusText, statusColor) = getStatusInfo(res.status_reservation_id)
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween) {
            Column {
                Text("Commande #${res.id}", fontWeight = FontWeight.Bold)
                Text(res.date, fontSize = 12.sp)
            }
            Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column(Modifier.padding(bottom = 8.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp)
        }
    }
}