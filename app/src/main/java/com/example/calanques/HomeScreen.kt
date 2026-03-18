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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesGrey
import com.example.calanques.ui.theme.CalanquesLightGrey
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

// --- 1. CARTE ---
@Composable
fun MapScreen() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                val startPoint = GeoPoint(43.2140, 5.4480)
                controller.setZoom(12.0)
                controller.setCenter(startPoint)
            }
        }
    )
}

// --- 2. STRUCTURE PRINCIPALE ---
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedActivite by remember { mutableStateOf<Activite?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Activités") },
                    label = { Text("Activités") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; selectedActivite = null },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = CalanquesBlue, selectedTextColor = CalanquesBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Panier") },
                    label = { Text("Panier") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = CalanquesBlue, selectedTextColor = CalanquesBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Compte") },
                    label = { Text("Compte") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = CalanquesBlue, selectedTextColor = CalanquesBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Carte") },
                    label = { Text("Carte") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = CalanquesBlue, selectedTextColor = CalanquesBlue)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> {
                    if (selectedActivite == null) {
                        HomeContent(onActiviteClick = { a -> selectedActivite = a })
                    } else {
                        DetailActiviteScreen(activite = selectedActivite!!, onBack = { selectedActivite = null })
                    }
                }
                1 -> PanierScreen()
                2 -> AccountScreen()
                3 -> MapScreen()
            }
        }
    }
}

// --- 3. CONTENU ACCUEIL ---
@Composable
fun HomeContent(onActiviteClick: (Activite) -> Unit) {
    val listeActivites = remember { mutableStateListOf<Activite>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val resultat = RetrofitClient.instance.getActivites()
            listeActivites.clear()
            listeActivites.addAll(resultat)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Erreur : ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(CalanquesLightGrey), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 16.dp), contentScale = ContentScale.Fit)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CalanquesBlue) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(listeActivites) { activite ->
                    ActiviteCard(activite = activite, onClick = { onActiviteClick(activite) })
                }
            }
        }
    }
}

// --- 4. CARTE D'ACTIVITÉ ---
@Composable
fun ActiviteCard(activite: Activite, onClick: () -> Unit) {
    val baseUrl = "http://webngo.sio.bts:8003/"
    val fullImageUrl = baseUrl + activite.image_url.removePrefix("/")

    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(model = fullImageUrl, contentDescription = activite.nom, modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = activite.nom, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = activite.description, fontSize = 13.sp, color = Color.Gray, maxLines = 2)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${activite.tarif} €", fontSize = 18.sp, color = CalanquesBlue, fontWeight = FontWeight.ExtraBold)
                    Surface(color = Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = activite.duree.split(":").let { if(it.size >= 2) "${it[0]}h${it[1]}" else activite.duree },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// --- 5. ÉCRAN DÉTAIL ---
@Composable
fun DetailActiviteScreen(activite: Activite, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = CalanquesBlue)) {
            Text("Retour")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = activite.nom, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = activite.description)
    }
}