package com.example.calanques

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// --- UTILITAIRE DE DATE ---
fun formatDateFr(dateStr: String?): String {
    if (dateStr == null) return "Date inconnue"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
        val date = parser.parse(dateStr)
        formatter.format(date!!)
    } catch (e: Exception) {
        dateStr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservation: ReservationResponse,
    allActivities: List<Activite>, // <-- CORRECTION : On passe la liste globale pour récupérer prix/noms
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Réservation #${reservation.id}", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CalanquesBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    Text("Récapitulatif", style = MaterialTheme.typography.labelLarge, color = CalanquesBlue)
                    Text(
                        text = "Effectuée le ${formatDateFr(reservation.date)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            item {
                Text("Activités incluses", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }

            // CORRECTION : Mapping des IDs vers les vraies données
            items(reservation.activities) { activity ->
                val infoReference = allActivities.find { it.id == activity.activity_id }

                // On crée une copie enrichie de l'activité avec le prix et le nom de la liste globale
                val enrichedActivity = activity.copy(
                    titre_activite = infoReference?.nom ?: "Activité #${activity.activity_id}",
                    prix_unitaire = infoReference?.tarif ?: 0.0
                )

                ActivityDetailCard(enrichedActivity)
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))

                if (reservation.status_reservation_id == 1) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CalanquesRed),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isCancelling
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Annuler ma réservation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CalanquesRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CalanquesRed.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Cette réservation est annulée",
                            color = CalanquesRed,
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CalanquesRed) },
            title = { Text("Confirmer l'annulation") },
            text = { Text("Souhaitez-vous vraiment annuler cette réservation ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        isCancelling = true
                        scope.launch {
                            try {
                                RetrofitClient.instance.updateReservationStatus("Bearer $token", reservation.id, StatusUpdateRequest(2))
                                onRefresh()
                                onBack()
                            } catch (e: Exception) {
                                isCancelling = false
                                Log.e("API_ERROR", "Erreur lors de l'annulation", e)
                            }
                        }
                    }
                ) {
                    Text("Oui, annuler", color = CalanquesRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Conserver", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ActivityDetailCard(activity: ReservationActivite) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp, 24.dp).clip(RoundedCornerShape(2.dp)).background(CalanquesBlue))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = activity.titre_activite ?: "Activité",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            InfoRow(icon = Icons.Default.CalendarMonth, text = "Le ${formatDateFr(activity.date)} à ${activity.heure?.take(5) ?: "--:--"}")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Default.Groups, text = "${activity.nb_participants} participant(s) x ${activity.prix_unitaire.toInt()} €")

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CalanquesBlue.copy(alpha = 0.08f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, null, tint = CalanquesBlue, modifier = Modifier.size(18.dp))
                    Text("  Total réglé", fontSize = 14.sp, color = CalanquesBlue)
                }
                Text(
                    text = "${activity.montant.toInt()} €",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CalanquesBlue
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
        Text(text = "  $text", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
    }
}