package com.example.calanques

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.material3.HorizontalDivider

// --- DIRECTION ARTISTIQUE OFFICIELLE ---
val CalanquesRed = Color(0xFFE51A2E)
val CalanquesGrey = Color(0xFF555555)

// 1. LE "CADRE" PRINCIPAL DE L'APPLICATION
@Composable
fun MainScreen() {
    // Variable qui retient l'onglet actif (0 par défaut = Activités)
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                // 0. Activités
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Activités") },
                    label = { Text("Activités") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }, // On change l'onglet actif
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesRed,
                        selectedTextColor = CalanquesRed,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesRed.copy(alpha = 0.1f)
                    )
                )
                // 1. Panier
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Panier") },
                    label = { Text("Panier") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }, // On change l'onglet actif
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesRed,
                        selectedTextColor = CalanquesRed,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesRed.copy(alpha = 0.1f)
                    )
                )
                // 2. Compte
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Compte") },
                    label = { Text("Compte") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }, // On change l'onglet actif
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesRed,
                        selectedTextColor = CalanquesRed,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesRed.copy(alpha = 0.1f)
                    )
                )
                // 3. Carte
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Carte") },
                    label = { Text("Carte") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }, // On change l'onglet actif
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
        // L'AIGUILLEUR : On affiche le contenu en fonction du clic
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeContent() // On affiche le logo et la liste
                1 -> Text("Page Panier (En construction)", modifier = Modifier.fillMaxSize().padding(16.dp))
                2 -> AccountScreen() // On appelle notre fichier AccountScreen.kt !
                3 -> Text("Page Carte (En construction)", modifier = Modifier.fillMaxSize().padding(16.dp))
            }
        }
    }
}

// 2. LE CONTENU DE L'ACCUEIL (Logo + future liste)
@Composable
fun HomeContent() {
    // 1. On crée une liste d'état qui va se mettre à jour quand l'API répond
    val listeActivites = remember { mutableStateListOf<Activite>() }
    var isLoading by remember { mutableStateOf(true) }

    // 2. On lance le chargement des données au démarrage (comme ton chargerDonnees())
    LaunchedEffect(Unit) {
        try {
            // On appelle l'API via ton objet Retrofit existant
            val resultat = RetrofitClient.instance.getActivites()
            listeActivites.clear()
            listeActivites.addAll(resultat)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Erreur lors du chargement : ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Officiel
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo du Parc National des Calanques",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            contentScale = ContentScale.Fit
        )

        // 3. Affichage conditionnel : Spinner si ça charge, sinon la liste
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CalanquesRed)
            }
        } else {
            // C'est ton nouveau RecyclerView en version Compose
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(listeActivites) { activite ->
                    ActiviteRow(activite) // Petit composant pour chaque ligne
                }
            }
        }
    }
}

@Composable
fun ActiviteRow(activite: Activite) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = activite.nom,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "${activite.tarif} €",
            fontSize = 14.sp,
            color = CalanquesRed
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
    }
}