package com.example.calanques

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    var selectedResDetail by remember { mutableStateOf<ReservationResponse?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedActivite by remember { mutableStateOf<Activite?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Activités") },
                    label = { Text("Activités") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; selectedActivite = null; selectedResDetail = null },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = CalanquesBlue, selectedTextColor = CalanquesBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Panier") },
                    label = { Text("Panier") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; selectedResDetail = null },
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
                    onClick = { selectedTab = 3; selectedResDetail = null },
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
                2 -> {
                    // Logique de navigation interne pour le profil : Liste ou Détails
                    if (selectedResDetail == null) {
                        AccountScreen(onReservationClick = { res -> selectedResDetail = res })
                    } else {
                        ReservationDetailScreen(
                            reservation = selectedResDetail!!,
                            onBack = { selectedResDetail = null }
                        )
                    }
                }
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
                    val tarifTxt = if (activite.tarif % 1 == 0.0) activite.tarif.toInt() else activite.tarif

                    Text(text = "$tarifTxt €", fontSize = 18.sp, color = CalanquesBlue, fontWeight = FontWeight.ExtraBold)

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailActiviteScreen(activite: Activite, onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    var nbParticipants by remember { mutableIntStateOf(1) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedHeure by remember { mutableStateOf("") }

    val quotaMax = 20
    val placesRestantes = quotaMax - nbParticipants
    val prixTotal = activite.tarif * nbParticipants

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activite.nom, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(id = R.drawable.arrow_left), contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            AsyncImage(
                model = "http://webngo.sio.bts:8004/${activite.image_url}",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(activite.nom, modifier = Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Icon(painterResource(id = R.drawable.clock_bold), null, modifier = Modifier.size(18.dp))
                    Text(" ${activite.duree}", fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(activite.description, fontSize = 14.sp, color = Color.DarkGray)
                Text("Règles : RDV 30 min avant le départ sur le quai.", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                Text("1. Choisir une date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                SelectionButton(
                    text = if (selectedDate.isEmpty()) "Sélectionner une date" else selectedDate,
                    icon = R.drawable.calendar_blank_bold
                ) {
                    selectedDate = "15 Juillet 2025"
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("2. Choisir l'horaire", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                SelectionButton(
                    text = if (selectedHeure.isEmpty()) "Sélectionner une heure" else selectedHeure,
                    icon = R.drawable.clock_bold,
                    enabled = selectedDate.isNotEmpty()
                ) {
                    selectedHeure = "10:00"
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Prix Total", fontSize = 12.sp, color = Color.Gray)
                        val prixTxt = if (prixTotal % 1 == 0.0) prixTotal.toInt() else prixTotal
                        Text("$prixTxt €", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CalanquesBlue)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Places restantes : $placesRestantes", fontSize = 12.sp, color = if(placesRestantes < 5) Color.Red else Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (nbParticipants > 1) nbParticipants-- }) {
                                Icon(painterResource(id = R.drawable.minus_circle), null, tint = CalanquesBlue)
                            }
                            Surface(border = BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(4.dp)) {
                                Text("$nbParticipants", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            }
                            IconButton(onClick = { if (nbParticipants < quotaMax) nbParticipants++ }) {
                                Icon(painterResource(id = R.drawable.plus_circle), null, tint = CalanquesBlue)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val isReady = selectedDate.isNotEmpty() && selectedHeure.isNotEmpty()

                Button(
                    onClick = { onBack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = isReady,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReady) CalanquesBlue else Color.LightGray
                    )
                ) {
                    Icon(painterResource(id = R.drawable.basket_bold), null)
                    Spacer(Modifier.width(12.dp))
                    Text("Ajouter au panier", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun SelectionButton(text: String, icon: Int, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (enabled) Color.LightGray else Color(0xFFEEEEEE)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Text(text, color = if (enabled) Color.Black else Color.LightGray)
        Spacer(Modifier.weight(1f))
        Icon(painterResource(id = icon), null, tint = if (enabled) CalanquesBlue else Color.LightGray)
    }
}