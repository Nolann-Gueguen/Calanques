package com.example.calanques

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calanques.ui.theme.CalanquesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du SessionManager pour vérifier la connexion
        val sessionManager = SessionManager(this)

        enableEdgeToEdge()
        setContent {
            CalanquesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Logique de départ : si pas de token -> login, sinon -> home
                    val startDestination = if (sessionManager.fetchAuthToken() == null) "login" else "home"

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        // --- ÉCRAN LOGIN ---
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    // On va vers l'accueil et on efface le login de l'historique
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToSignUp = {
                                    navController.navigate("signup")
                                }
                            )
                        }

                        // --- ÉCRAN SIGNUP ---
                        composable("signup") {
                            SignUpScreen(
                                onSignUpSuccess = {
                                    navController.navigate("login")
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // --- ÉCRAN ACCUEIL (Avec gestion du rôle) ---
                        composable("home") {
                            val roleId = sessionManager.getUserRole() // Récupère 1 (Client) ou 2 (Admin)

                            HomeScreen(
                                roleId = roleId,
                                onLogout = {
                                    sessionManager.clearSession()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}