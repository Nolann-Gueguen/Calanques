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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesLightGrey
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// ---------------------------------------------------------------------------
// COORDONNÉES GPS FIXES PAR ACTIVITÉ (id BDD → lat, lon)
// ---------------------------------------------------------------------------
val coordonneesActivites = mapOf(
    1  to Pair(43.2140, 5.5424), // Excursion en bateau        – Port de Cassis
    2  to Pair(43.2192, 5.4502), // Randonnée Sugiton guidée   – Calanque de Sugiton
    3  to Pair(43.2250, 5.4300), // Sortie VTT                 – Sentiers du Parc
    4  to Pair(43.2112, 5.4178), // Plongée sous-marine        – Calanque de Morgiou
    5  to Pair(43.1981, 5.5006), // Kayak guidé                – Calanque d'En-Vau
    6  to Pair(43.2089, 5.3503), // Visite grotte Cosquer      – Cap Morgiou
    7  to Pair(43.2030, 5.4210), // Escalade encadrée          – Falaises de Sormiou
    8  to Pair(43.2278, 5.4389), // Parcours accrobranche      – Parc des Calanques
    9  to Pair(43.2310, 5.4551), // Séance de yoga             – Domaine de Luminy
    10 to Pair(43.2192, 5.4502), // Bivouac réglementé         – Zone Sugiton
    11 to Pair(43.4367, 5.2150), // Vol en hélicoptère         – Héliport Marseille
    12 to Pair(43.2089, 5.3503), // Initiation spéléologie     – Grottes Cap Morgiou
    13 to Pair(43.1981, 5.5006), // Randonnée Belvédère En Vau – En-Vau
    14 to Pair(43.1981, 5.5006), // Sortie kayak sunset        – En-Vau
    15 to Pair(43.2310, 5.4551), // Bain sonore méditatif      – Luminy
    16 to Pair(43.2112, 5.4178), // Exploration en paddle      – Morgiou
    17 to Pair(43.2030, 5.4210), // Escalade grande voie       – Sormiou
    18 to Pair(43.2250, 5.4300), // Atelier photo nature       – Sentiers du Parc
    19 to Pair(43.2192, 5.4502)  // Randonnée botanique        – Sugiton
)

// --- 1. CARTE AVEC MARQUEURS CLIQUABLES ---
@Composable
fun MapScreen(
    listeActivites: List<Activite>,
    onActiviteClick: (Activite) -> Unit
) {
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
                controller.setZoom(12.5)
                controller.setCenter(GeoPoint(43.2140, 5.4300))
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            listeActivites.forEach { activite ->
                val coords = coordonneesActivites[activite.id] ?: return@forEach

                val tarifTxt = if (activite.tarif % 1 == 0.0)
                    activite.tarif.toInt().toString()
                else
                    activite.tarif.toString()

                val dureeTxt = activite.duree.split(":").let {
                    if (it.size >= 2) "${it[0]}h${it[1]}" else activite.duree
                }

                val resume = activite.description.take(90) +
                        if (activite.description.length > 90) "…" else ""

                val marker = Marker(mapView).apply {
                    position = GeoPoint(coords.first, coords.second)
                    title = activite.nom
                    snippet = "$tarifTxt € · $dureeTxt\n$resume"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    setOnMarkerClickListener { m, _ ->
                        if (m.isInfoWindowShown) {
                            // 2e clic sur la bulle → ouvre le détail complet
                            onActiviteClick(activite)
                        } else {
                            // 1er clic → ferme les autres bulles et ouvre celle-ci
                            mapView.overlays
                                .filterIsInstance<Marker>()
                                .forEach { it.closeInfoWindow() }
                            m.showInfoWindow()
                        }
                        true
                    }
                }
                mapView.overlays.add(marker)
            }

            mapView.invalidate()
        }
    )
}

// --- 2. STRUCTURE PRINCIPALE ---
@Composable
fun HomeScreen(roleId: Int, onLogout: () -> Unit) {
    var selectedResDetail by remember { mutableStateOf<ReservationResponse?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedActivite by remember { mutableStateOf<Activite?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Activités chargées une seule fois, partagées avec la carte
    val listeActivites = remember { mutableStateListOf<Activite>() }

    LaunchedEffect(Unit) {
        try {
            val activites = RetrofitClient.instance.getActivites()
            listeActivites.clear()
            listeActivites.addAll(activites)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Erreur chargement activités : ${e.message}")
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Activités") },
                    label = { Text("Activités") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; selectedActivite = null; selectedResDetail = null },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesBlue,
                        selectedTextColor = CalanquesBlue
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Panier") },
                    label = { Text("Panier") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; selectedResDetail = null },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesBlue,
                        selectedTextColor = CalanquesBlue
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Compte") },
                    label = { Text("Compte") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesBlue,
                        selectedTextColor = CalanquesBlue
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Carte") },
                    label = { Text("Carte") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3; selectedResDetail = null },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalanquesBlue,
                        selectedTextColor = CalanquesBlue
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> {
                    if (selectedActivite == null) {
                        HomeContent(
                            roleId = roleId,
                            listeActivitesParent = listeActivites,
                            onActiviteClick = { a -> selectedActivite = a },
                            onLogout = onLogout
                        )
                    } else {
                        DetailActiviteScreen(
                            activiteId = selectedActivite!!.id,
                            onBack = { selectedActivite = null }
                        )
                    }
                }
                1 -> PanierScreen()
                2 -> {
                    if (selectedResDetail == null) {
                        key(refreshTrigger) {
                            AccountScreen(
                                onReservationClick = { res -> selectedResDetail = res },
                                onLogout = onLogout
                            )
                        }
                    } else {
                        ReservationDetailScreen(
                            reservation = selectedResDetail!!,
                            onBack = { selectedResDetail = null },
                            onRefresh = {
                                refreshTrigger++
                                selectedResDetail = null
                            }
                        )
                    }
                }
                3 -> {
                    // Clic sur un marqueur → détail ; retour → revient à la carte
                    if (selectedActivite != null) {
                        DetailActiviteScreen(
                            activiteId = selectedActivite!!.id,
                            onBack = {
                                selectedActivite = null
                                selectedTab = 3
                            }
                        )
                    } else {
                        MapScreen(
                            listeActivites = listeActivites,
                            onActiviteClick = { activite -> selectedActivite = activite }
                        )
                    }
                }
            }
        }
    }
}

// --- 3. CONTENU ACCUEIL ---
@Composable
fun HomeContent(
    roleId: Int,
    listeActivitesParent: List<Activite>,
    onActiviteClick: (Activite) -> Unit,
    onLogout: () -> Unit
) {
    val listeActivites = remember { mutableStateListOf<Activite>() }
    val listeTypes = remember { mutableStateListOf<TypeActivite>() }
    var isLoading by remember { mutableStateOf(true) }
    var selectedType by remember { mutableStateOf<TypeActivite?>(null) }

    LaunchedEffect(Unit) {
        try {
            if (listeActivitesParent.isNotEmpty()) {
                listeActivites.clear()
                listeActivites.addAll(listeActivitesParent)
            } else {
                val activites = RetrofitClient.instance.getActivites()
                listeActivites.clear()
                listeActivites.addAll(activites)
            }

            val types = RetrofitClient.instance.getTypesActivites()
            listeTypes.clear()
            listeTypes.addAll(types)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Erreur chargement : ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Si un type est sélectionné → on affiche ses activités
    if (selectedType != null) {
        val activitesDuType = listeActivites.filter { it.type_id == selectedType!!.id }
        ActivitesDuTypeScreen(
            type = selectedType!!,
            activites = activitesDuType,
            onActiviteClick = onActiviteClick,
            onBack = { selectedType = null }
        )
        return
    }

    // Écran principal : liste des types
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalanquesLightGrey),
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
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Nos catégories",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CalanquesBlue,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Choisissez une catégorie d'activité",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(listeTypes) { type ->
                    val nbActivites = listeActivites.count { it.type_id == type.id }
                    TypeActiviteCard(
                        type = type,
                        nbActivites = nbActivites,
                        onClick = { selectedType = type }
                    )
                }
            }
        }
    }
}

// --- CARTE D'UN TYPE D'ACTIVITÉ ---
@Composable
fun TypeActiviteCard(type: TypeActivite, nbActivites: Int, onClick: () -> Unit) {
    val baseUrl = "http://webngo.sio.bts:8003/"
    val imageUrl = if (!type.image_url.isNullOrBlank())
        baseUrl + type.image_url.removePrefix("/")
    else null

    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = type.libelle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CalanquesBlue.copy(alpha = 0.15f))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            ),
                            startY = 60f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = type.libelle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = CalanquesBlue.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$nbActivites activité${if (nbActivites > 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// --- ÉCRAN ACTIVITÉS D'UN TYPE ---
@Composable
fun ActivitesDuTypeScreen(
    type: TypeActivite,
    activites: List<Activite>,
    onActiviteClick: (Activite) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalanquesLightGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalanquesBlue)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "Retour",
                    tint = Color.White
                )
            }
            Text(
                text = type.libelle,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        if (activites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Aucune activité disponible pour cette catégorie.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(activites) { activite ->
                    ActiviteCard(activite = activite, onClick = { onActiviteClick(activite) })
                }
            }
        }
    }
}

// --- CARTE D'ACTIVITÉ ---
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
            AsyncImage(
                model = fullImageUrl,
                contentDescription = activite.nom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = activite.nom,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
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
                    val tarifTxt = if (activite.tarif % 1 == 0.0) activite.tarif.toInt() else activite.tarif
                    Text(
                        text = "$tarifTxt €",
                        fontSize = 18.sp,
                        color = CalanquesBlue,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = activite.duree.split(":").let {
                                if (it.size >= 2) "${it[0]}h${it[1]}" else activite.duree
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}