package com.example.calanques

import androidx.compose.foundation.layout.*
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
    var isLoading by remember { mutableStateOf(userProfileState == null) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(userProfileState) {
        if (userProfileState == null) {
            try {
                isLoading = true
                val response = RetrofitClient.instance.getMe("Bearer $token")
                userProfile = response
                onProfileLoaded(response)
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Impossible de récupérer vos informations."
            }
        } else {
            userProfile = userProfileState
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CalanquesLightGrey),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Détails du compte", fontWeight = FontWeight.Bold, color = CalanquesBlue, modifier = Modifier.padding(bottom = 12.dp))

                    // Affichage complet de toutes les infos
                    InfoRow(label = "Prénom", value = user.prenom)
                    InfoRow(label = "Nom", value = user.nom)
                    InfoRow(label = "E-mail", value = user.email)
                    InfoRow(label = "Téléphone", value = user.telephone, icon = "📞")

                    val fullAddress = "${user.adresse ?: ""}\n${user.cp ?: ""} ${user.ville ?: ""}".trim()
                    if (fullAddress.length > 5) {
                        InfoRow(label = "Adresse", value = fullAddress, icon = "📍")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { onEditProfile(user) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CalanquesBlue))
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = CalanquesBlue, modifier = Modifier.padding(end = 8.dp))
                Text("Modifier mes informations", color = CalanquesBlue)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onLogout() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CalanquesRed)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Se déconnecter", fontSize = 16.sp, color = Color.White)
        }
    }
}

// Petite fonction utilitaire pour l'affichage propre des lignes d'info
@Composable
fun InfoRow(label: String, value: String?, icon: String? = null) {
    if (!value.isNullOrBlank()) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = "${icon ?: ""} $value".trim(), fontSize = 14.sp, color = Color.Black)
        }
    }
}