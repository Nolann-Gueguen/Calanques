package com.example.calanques

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// --- DIRECTION ARTISTIQUE OFFICIELLE ---
val CalanquesRed = Color(0xFFE51A2E) // Couleur principale
val CalanquesGrey = Color(0xFF555555) // Couleur secondaire

@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White // Fond blanc pur
            ) {
                // 1. Activités
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Activités") },
                    label = { Text("Activités") },
                    selected = true, // Onglet actif par défaut sur l'accueil
                    onClick = { /* Action au clic */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesRed,
                        selectedTextColor = CalanquesRed,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesRed.copy(alpha = 0.1f) // Léger fond rouge très pro
                    )
                )
                // 2. Panier
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Panier") },
                    label = { Text("Panier") },
                    selected = false,
                    onClick = { /* Action au clic */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesRed,
                        selectedTextColor = CalanquesRed,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesRed.copy(alpha = 0.1f)
                    )
                )
                // 3. Compte
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Compte") },
                    label = { Text("Compte") },
                    selected = false,
                    onClick = { /* Action au clic */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesRed,
                        selectedTextColor = CalanquesRed,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesRed.copy(alpha = 0.1f)
                    )
                )
                // 4. Carte
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Carte") },
                    label = { Text("Carte") },
                    selected = false,
                    onClick = { /* Action au clic */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesRed,
                        selectedTextColor = CalanquesRed,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesRed.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Ici viendront le Logo et la Liste des types d'activités")
        }
    }
}