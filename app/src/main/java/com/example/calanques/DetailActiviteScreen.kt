package com.example.calanques

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.calanques.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesGrey
import com.example.calanques.ui.theme.CalanquesLightGrey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailActiviteScreen(activiteId: Int, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var activite by remember { mutableStateOf<Activite?>(null) }
    var nbParticipants by remember { mutableIntStateOf(1) }
    var selectedDate by remember { mutableStateOf("Sélectionner une date") }
    var selectedHeure by remember { mutableStateOf("Sélectionner une heure") }
    var isLoading by remember { mutableStateOf(true) }

    // Chargement des détails depuis l'API
    LaunchedEffect(activiteId) {
        try {
            activite = RetrofitClient.instance.getActivityDetail(activiteId)
        } catch (e: Exception) {
            Log.e("DetailScreen", "Erreur de chargement", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(activite?.nom ?: "Détails", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_left),
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CalanquesBlue)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CalanquesBlue)
            }
        } else {
            activite?.let { detail ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // --- CHARGEMENT DE L'IMAGE VIA L'URL DE LA TABLE ACTIVITÉS ---
                    // On combine l'URL de base du serveur et le champ image_url de la BDD
                    AsyncImage(
                        model = "http://webngo.sio.bts:8004/${detail.image_url}", // Image distante
                        contentDescription = null,
                        placeholder = painterResource(id = R.drawable.chargement), // Image locale temporaire
                        error = painterResource(id = R.drawable.chargement),
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(20.dp)) {
                        // Nom et Durée provenant de la BDD
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(detail.nom, style = CustomTypography.titleLarge, fontSize = 22.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(id = R.drawable.clock_bold), null, modifier = Modifier.size(18.dp))
                                Text(" ${detail.duree}", style = CustomTypography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Description
                        Text(detail.description, style = CustomTypography.bodyMedium)
                        Text("RDV 30 min avant le départ", fontSize = 12.sp, color = CalanquesGrey)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Boutons de sélection
                        SelectionButton(text = selectedDate, icon = R.drawable.calendar_blank_bold) {
                            selectedDate = "2025-07-15"
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SelectionButton(text = selectedHeure, icon = R.drawable.clock_bold) {
                            selectedHeure = "10:00:00"
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tarif de l'activité
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${detail.tarif} €", style = CustomTypography.titleLarge, fontSize = 28.sp)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (nbParticipants > 1) nbParticipants-- }) {
                                    Icon(painterResource(id = R.drawable.minus_circle), null, tint = CalanquesBlue)
                                }
                                Text("$nbParticipants", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                IconButton(onClick = { nbParticipants++ }) {
                                    Icon(painterResource(id = R.drawable.plus_circle), null, tint = CalanquesBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Ajout au panier via l'API
                        Button(
                            onClick = {
                                val reservation = ReservationCreate(
                                    date = "2026-03-11",
                                    activities = listOf(
                                        ReservationActiviteCreate(
                                            activity_id = detail.id,
                                            date = selectedDate,
                                            heure = selectedHeure,
                                            nb_participants = nbParticipants
                                        )
                                    )
                                )
                                scope.launch {
                                    try {
                                        RetrofitClient.instance.createReservation(reservation)
                                        onBack()
                                    } catch (e: Exception) {
                                        Log.e("POST", "Erreur réservation", e)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CalanquesBlue)
                        ) {
                            Icon(painterResource(id = R.drawable.basket_bold), null)
                            Spacer(Modifier.width(12.dp))
                            Text("Ajouter au panier", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionButton(text: String, icon: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CalanquesGrey)
    ) {
        Text(text)
        Spacer(Modifier.weight(1f))
        Icon(painterResource(id = icon), null, tint = CalanquesBlue)
    }
}