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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
            // Section Récapitulatif
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

            // Liste des activités réservées
            items(reservation.activities) { activity ->
                ActivityDetailCard(activity)
            }

            // Section Action : Annulation
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Statut 1 = Confirmée (selon ton schéma SQL)
                if (reservation.status_reservation_id == 1) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CalanquesRed),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isCancelling
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Annuler ma réservation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    // Affichage si le statut est déjà à 2 (Annulée)
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
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGUE DE CONFIRMATION ---
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CalanquesRed) },
            title = { Text("Confirmer l'annulation") },
            text = { Text("Souhaitez-vous vraiment annuler cette réservation ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        isCancelling = true
                        scope.launch {
                            try {
                                // Mise à jour vers le statut_reservation_id = 2 (Annulée)
                                RetrofitClient.instance.updateReservationStatus(
                                    "Bearer $token",
                                    reservation.id,
                                    StatusUpdateRequest(2)
                                )
                                onRefresh() // Déclenche le refresh du profil
                                onBack()    // Ferme l'écran
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
    // Calcul précis du montant total
    val totalCalcule = activity.nb_participants * activity.prix_unitaire

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

            // Ligne Date et Heure
            InfoRow(
                icon = Icons.Default.CalendarMonth,
                text = "Le ${formatDateFr(activity.date)} à ${activity.heure?.take(5) ?: "--:--"}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ligne Participants + Tarif Unitaire (pour vérifier le calcul)
            InfoRow(
                icon = Icons.Default.Groups,
                text = "${activity.nb_participants} participant(s) x ${activity.prix_unitaire.toInt()} €"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Badge du Total Réglé
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
                    text = "${totalCalcule.toInt()} €",
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