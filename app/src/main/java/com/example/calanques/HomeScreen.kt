package com.example.calanques

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesGrey
import com.example.calanques.ui.theme.CalanquesLightGrey

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
                        selectedIconColor = CalanquesBlue,
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
                        selectedIconColor = CalanquesBlue,
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
                        selectedIconColor = CalanquesBlue,
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

// --- 2. CONTENU ACCUEIL ---
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
            errorMessage = "Le serveur ne répond pas.\nVérifiez que WampServer est lancé."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalanquesLightGrey), // Fond gris clair comme le panier
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 16.dp),
            contentScale = ContentScale.Fit
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CalanquesBlue)
            }
        } else if (errorMessage != null) {
            // ... (Code d'erreur inchangé)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(listeActivites) { activite ->
                    ActiviteCard(activite)
                }
            }
        }
    }
}

// --- 3. CARTE D'ACTIVITÉ (DESIGN AVEC IMAGE) ---
@Composable
fun ActiviteCard(activite: Activite) {
    // Construction de l'URL de l'image
    val baseUrl = "http://webngo.sio.bts:8003/"
    val fullImageUrl = baseUrl + activite.image_url.removePrefix("/")

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp), // Coins arrondis comme dans PanierScreen
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column {
            // AFFICHAGE DE L'IMAGE VIA COIL
            AsyncImage(
                model = fullImageUrl,
                contentDescription = activite.nom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop, // L'image remplit bien l'espace
                placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                error = painterResource(android.R.drawable.ic_menu_report_image)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = activite.nom,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description (si elle existe dans ton objet Activite)
                Text(
                    text = activite.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${activite.tarif} €",
                        fontSize = 18.sp,
                        color = CalanquesBlue,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Surface(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = activite.duree,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}