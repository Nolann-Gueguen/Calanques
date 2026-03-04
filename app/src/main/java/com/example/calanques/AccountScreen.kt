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

@Composable
fun AccountScreen() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isSigningUp by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        ProfileScreen(onLogout = { isLoggedIn = false })
    } else {
        if (isSigningUp) {
            // C'est ici que la magie opère
            SignUpScreen(
                onSignUpSuccess = {
                    isSigningUp = false
                    isLoggedIn = true
                },
                // Quand l'utilisateur clique sur "Déjà un compte ?",
                // on repasse isSigningUp à false pour revenir au LoginScreen
                onNavigateToLogin = { isSigningUp = false }
            )
        } else {
            LoginScreen(
                onLoginSuccess = { isLoggedIn = true },
                onNavigateToSignUp = { isSigningUp = true }
            )
        }
    }
}

@Composable
fun ProfileScreen(onLogout: () -> Unit, onEditProfile: () -> Unit = {}) {
    val calanquesRed = Color(0xFFE51A2E)
    val lightGrey = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp),
            tint = Color.Gray
        )

        // Données issues de ton SQL (Alice Dupont)
        Text(text = "Alice Dupont", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "alice.dupont@gmail.com", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // Carte d'informations détaillées
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = lightGrey),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Informations de contact",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(text = "📍 10 rue Paradis, 13001 Marseille", modifier = Modifier.padding(bottom = 8.dp))
                Text(text = "📞 06 00 00 00 01", modifier = Modifier.padding(bottom = 8.dp))
                // Affichage du rôle récupéré depuis user_roles
                Text(text = "👤 Rôle : Client", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { onEditProfile() }, // Déclenche l'action de modification
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Modifier mes informations", color = Color.Black)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onLogout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = calanquesRed)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Se déconnecter", fontSize = 16.sp, color = Color.White)
        }
    }
}