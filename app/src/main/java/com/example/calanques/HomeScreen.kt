package com.example.calanques

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesGrey

// --- 1. STRUCTURE PRINCIPALE (AIGUILLEUR) ---
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Activités") },
                    label = { Text("Activités") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesBlue, // Passage au Bleu
                        selectedTextColor = CalanquesBlue,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Panier") },
                    label = { Text("Panier") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesBlue, // Passage au Bleu
                        selectedTextColor = CalanquesBlue,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Compte") },
                    label = { Text("Compte") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesBlue, // Passage au Bleu
                        selectedTextColor = CalanquesBlue,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Carte") },
                    label = { Text("Carte") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesBlue, // Passage au Bleu
                        selectedTextColor = CalanquesBlue,
                        unselectedIconColor = CalanquesGrey,
                        unselectedTextColor = CalanquesGrey,
                        indicatorColor = CalanquesBlue.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeContent()
                1 -> PanierScreen()
                2 -> AccountScreen()
                3 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Page Carte (En construction)", color = CalanquesGrey)
                }
            }
        }
    }
}

// --- 2. CONTENU ACCUEIL (LISTE DES 19 ACTIVITÉS) ---
@Composable
fun HomeContent() {
    val listeActivites = remember { mutableStateListOf<Activite>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        errorMessage = null
        try {
            val resultat = RetrofitClient.instance.getActivites()
            listeActivites.clear()
            listeActivites.addAll(resultat)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Erreur : ${e.message}")
            errorMessage = "Le serveur ne répond pas.\nVérifiez votre connexion."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo du Parc National des Calanques",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            contentScale = ContentScale.Fit
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CalanquesBlue) // Loader Bleu
            }
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { refreshTrigger++ },
                    colors = ButtonDefaults.buttonColors(containerColor = CalanquesBlue) // Bouton Bleu
                ) {
                    Text("Réessayer", color = Color.White)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listeActivites) { activite ->
                    ActiviteRow(activite)
                }
            }
        }
    }
}

// --- 3. LIGNE D'ACTIVITÉ (DESIGN) ---
@Composable
fun ActiviteRow(activite: Activite) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = activite.nom,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${activite.tarif} €",
                fontSize = 14.sp,
                color = CalanquesBlue, // Prix en Bleu
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activite.duree,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            thickness = 0.5.dp,
            color = Color.LightGray
        )
    }
}