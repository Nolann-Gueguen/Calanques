package com.example.calanques

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesRed
import com.example.calanques.ui.theme.CalanquesGrey
import com.example.calanques.ui.theme.CalanquesLightGrey
import kotlinx.coroutines.launch

val CustomTypography = Typography(
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, color = CalanquesGrey)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanierScreen(refreshKey: Int = 0) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    // Récupère le token depuis le SessionManager (même système que AccountScreen)
    val token = sessionManager.fetchAuthToken() ?: ""

    val reservations = remember { mutableStateListOf<ReservationResponse>() }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Se relance à chaque fois que refreshKey change (après un ajout au panier)
    LaunchedEffect(refreshKey) {
        isLoading = true
        reservations.clear()

        // On vérifie qu'on est connecté avant de contacter l'API
        if (token.isNotEmpty()) {
            try {
                val response = RetrofitClient.instance.getMyReservations(token)
                reservations.addAll(response.filter { it.status_reservation_id == 1 })
            } catch (e: Exception) {
                Log.e("PanierScreen", "Erreur lors de la récupération : ${e.message}")
            }
        }

        isLoading = false
    }

    val totalGlobal = reservations.sumOf { res -> res.activities.sumOf { it.montant } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mon Panier", style = CustomTypography.titleLarge.copy(color = Color.White)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CalanquesBlue)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 15.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total à régler", style = CustomTypography.bodyMedium)
                        val totalDisplay = if (totalGlobal % 1 == 0.0) totalGlobal.toInt().toString() else totalGlobal.toString()
                        Text("$totalDisplay €", style = CustomTypography.titleLarge.copy(fontSize = 26.sp), color = CalanquesBlue)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CalanquesBlue)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.basket_bold), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text("RÉSERVER", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = CalanquesBlue)
            } else if (reservations.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Votre panier est vide", style = CustomTypography.bodyMedium)
                    Text("Ajoutez des activités pour commencer", fontSize = 13.sp, color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(CalanquesLightGrey),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reservations) { reservation ->
                        reservation.activities.forEach { activite ->
                            ItemPanier(
                                titre = activite.titre_activite ?: "Activité Calanques",
                                date = "${activite.date} à ${activite.heure}",
                                nbParticipants = activite.nb_participants,
                                montant = activite.montant,
                                onDelete = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.deleteReservation(token, reservation.id)
                                            reservations.remove(reservation)
                                        } catch (e: Exception) {
                                            Log.e("Panier", "Erreur suppression BDD", e)
                                        }
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

@Composable
fun ItemPanier(titre: String, date: String, nbParticipants: Int, montant: Double, onDelete: () -> Unit) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = titre, style = CustomTypography.titleLarge, color = CalanquesBlue, modifier = Modifier.fillMaxWidth(0.85f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.calendar_blank_bold), contentDescription = null, modifier = Modifier.size(16.dp), tint = CalanquesGrey)
                    Text("  $date", style = CustomTypography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = R.drawable.user_bold), contentDescription = null, modifier = Modifier.size(18.dp), tint = CalanquesBlue)
                        Text("  $nbParticipants pers.", style = CustomTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black))
                    }
                    val montantDisplay = if (montant % 1 == 0.0) montant.toInt().toString() else montant.toString()
                    Text("$montantDisplay €", style = CustomTypography.titleLarge.copy(fontSize = 22.sp), color = Color.Black)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                Icon(painter = painterResource(id = R.drawable.trash_bold), contentDescription = "Supprimer", tint = CalanquesRed, modifier = Modifier.size(22.dp))
            }
        }
    }
}