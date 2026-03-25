package com.example.calanques

import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.calanques.ui.theme.CalanquesBlue
import com.example.calanques.ui.theme.CalanquesGrey
import com.example.calanques.ui.theme.CalanquesLightGrey
import com.example.calanques.ui.theme.CalanquesRed
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// --- 1. Utilitaires de Date ---
object DateUtils {
    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun convertMillisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun formatToDisplay(date: LocalDate): String = date.format(displayFormatter)
    fun formatToApi(date: LocalDate): String = date.format(apiFormatter)
}

// --- 2. Composant DatePicker Material 3 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDatePicker(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // Minuit aujourd'hui en UTC pour bloquer les dates passées
    val today = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= today
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(DateUtils.convertMillisToLocalDate(it))
                }
                onDismiss()
            }) {
                Text("Confirmer", color = CalanquesBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CalanquesGrey)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White // Optionnel: force le fond blanc si besoin
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = CalanquesBlue,
                todayDateBorderColor = CalanquesBlue,
                todayContentColor = CalanquesBlue
            )
        )
    }
}

// --- 3. Écran Principal ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailActiviteScreen(activiteId: Int, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var activite by remember { mutableStateOf<Activite?>(null) }
    var nbParticipants by remember { mutableIntStateOf(1) }

    // --- ETAT POUR LE DATEPICKER COMPOSE ---
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedDateDisplay by remember { mutableStateOf("Choisir une date") }

    // --- HEURE : null = pas encore choisie ---
    var selectedHeure by remember { mutableStateOf<String?>(null) }
    var selectedHeureDisplay by remember { mutableStateOf("Choisir une heure") }

    var isLoading by remember { mutableStateOf(true) }
    var reservationSuccess by remember { mutableStateOf(false) }
    var reservationError by remember { mutableStateOf(false) }

    // Animation d'apparition
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(40f) }

    // Calcul du total
    val totalPrice = remember(nbParticipants, activite) {
        (activite?.tarif ?: 0.0) * nbParticipants
    }

    LaunchedEffect(activiteId) {
        try {
            activite = RetrofitClient.instance.getActivityDetail(activiteId)
        } catch (e: Exception) {
            Log.e("DetailScreen", "Erreur de chargement", e)
        } finally {
            isLoading = false
            contentAlpha.animateTo(1f, tween(500))
            contentOffsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    // --- TIME PICKER NATIF ANDROID ---
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedHeure = String.format("%02d:%02d:00", hour, minute)
            selectedHeureDisplay = String.format("%02dh%02d", hour, minute)
        },
        9, 0, true
    )

    Box(modifier = Modifier.fillMaxSize().background(CalanquesLightGrey)) {

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = CalanquesBlue,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Chargement...", color = CalanquesGrey, fontSize = 14.sp)
                }
            }
        } else {
            activite?.let { detail ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .graphicsLayer {
                            alpha = contentAlpha.value
                            translationY = contentOffsetY.value
                        }
                ) {
                    // ========== HERO IMAGE ==========
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = "http://webngo.sio.bts:8003/${detail.image_url.removePrefix("/")}",
                            contentDescription = detail.nom,
                            placeholder = painterResource(id = R.drawable.chargement),
                            error = painterResource(id = R.drawable.chargement),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay du bas
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        ),
                                        startY = 100f
                                    )
                                )
                        )

                        // Bouton retour flottant
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                                .size(44.dp)
                                .shadow(8.dp, CircleShape)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = CalanquesBlue
                            )
                        }

                        // Titre sur l'image
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = detail.nom,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                // Badge durée
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            painterResource(id = R.drawable.clock_bold),
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = detail.duree.split(":").let {
                                                if (it.size >= 2) "${it[0]}h${it[1]}" else detail.duree
                                            },
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Badge tarif
                                Surface(
                                    color = CalanquesBlue.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    val tarifTxt = if (detail.tarif % 1 == 0.0) detail.tarif.toInt().toString() else detail.tarif.toString()
                                    Text(
                                        text = "$tarifTxt € / pers.",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ========== CONTENU PRINCIPAL ==========
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .padding(24.dp)
                    ) {

                        // --- Description ---
                        Text(
                            text = "À propos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalanquesBlue,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = detail.description,
                            fontSize = 14.sp,
                            color = CalanquesGrey,
                            lineHeight = 22.sp
                        )

                        // Note RDV
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            color = CalanquesBlue.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text("ℹ️", fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "RDV 30 min avant le départ au point de rendez-vous",
                                    fontSize = 12.sp,
                                    color = CalanquesBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                        Divider(color = CalanquesLightGrey, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(28.dp))

                        // --- Réservation ---
                        Text(
                            text = "Votre réservation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalanquesBlue,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Sélecteur DATE
                        PickerCard(
                            label = "Date",
                            value = selectedDateDisplay,
                            iconRes = R.drawable.calendar_blank_bold,
                            isSelected = selectedDate != null,
                            onClick = { showDatePicker = true } // Changé ici !
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sélecteur HEURE
                        PickerCard(
                            label = "Heure",
                            value = selectedHeureDisplay,
                            iconRes = R.drawable.clock_bold,
                            isSelected = selectedHeure != null,
                            onClick = { timePickerDialog.show() }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- Nombre de participants ---
                        Text(
                            text = "Participants",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalanquesBlue,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CalanquesLightGrey)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { if (nbParticipants > 1) nbParticipants-- },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (nbParticipants > 1) CalanquesBlue else Color.LightGray)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.minus_circle),
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$nbParticipants",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp,
                                    color = CalanquesBlue
                                )
                                Text(
                                    if (nbParticipants > 1) "personnes" else "personne",
                                    fontSize = 11.sp,
                                    color = CalanquesGrey
                                )
                            }

                            IconButton(
                                onClick = { nbParticipants++ },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(CalanquesBlue)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.plus_circle),
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                        Divider(color = CalanquesLightGrey, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        // --- Récapitulatif du total ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total estimé", fontSize = 13.sp, color = CalanquesGrey)
                                val tarifTxt = if (detail.tarif % 1 == 0.0) detail.tarif.toInt().toString() else detail.tarif.toString()
                                Text(
                                    "$tarifTxt € × $nbParticipants pers.",
                                    fontSize = 12.sp,
                                    color = CalanquesGrey
                                )
                            }
                            val totalDisplay = if (totalPrice % 1 == 0.0) totalPrice.toInt().toString() else totalPrice.toString()
                            Text(
                                "$totalDisplay €",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CalanquesBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // --- Feedback succès/erreur ---
                        AnimatedVisibility(visible = reservationSuccess) {
                            Surface(
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Text(
                                    "✅ Activité ajoutée au panier !",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = reservationError) {
                            Surface(
                                color = CalanquesRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Text(
                                    "❌ Erreur lors de l'ajout. Vérifiez votre connexion.",
                                    color = CalanquesRed,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // --- Bouton Ajouter au panier ---
                        val canAdd = selectedDate != null && selectedHeure != null
                        Button(
                            onClick = {
                                if (!canAdd) return@Button
                                val reservation = ReservationCreate(
                                    date = selectedDate!!,
                                    activities = listOf(
                                        ReservationActiviteCreate(
                                            activity_id = detail.id,
                                            date = selectedDate!!,
                                            heure = selectedHeure!!,
                                            nb_participants = nbParticipants
                                        )
                                    )
                                )
                                scope.launch {
                                    try {
                                        RetrofitClient.instance.createReservation(reservation)
                                        reservationSuccess = true
                                        reservationError = false
                                        kotlinx.coroutines.delay(1500)
                                        onBack()
                                    } catch (e: Exception) {
                                        Log.e("POST", "Erreur réservation", e)
                                        reservationError = true
                                        reservationSuccess = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAdd) CalanquesBlue else Color.LightGray,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (canAdd) 6.dp else 0.dp
                            )
                        ) {
                            Icon(
                                painterResource(id = R.drawable.basket_bold),
                                null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (canAdd) "Ajouter au panier" else "Choisissez date et heure",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // --- AFFICHAGE CONDITIONNEL DU DATEPICKER ---
        if (showDatePicker) {
            ModernDatePicker(
                onDateSelected = { date ->
                    selectedDate = DateUtils.formatToApi(date)
                    selectedDateDisplay = DateUtils.formatToDisplay(date)
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

// ========== COMPOSANT CARTE DE SÉLECTION ==========
@Composable
fun PickerCard(
    label: String,
    value: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) CalanquesBlue else Color.LightGray
    val bgColor = if (isSelected) CalanquesBlue.copy(alpha = 0.05f) else Color.White

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône dans un cercle coloré
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CalanquesBlue else CalanquesLightGrey),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(id = iconRes),
                    null,
                    tint = if (isSelected) Color.White else CalanquesGrey,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 11.sp,
                    color = if (isSelected) CalanquesBlue else CalanquesGrey,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    value,
                    fontSize = 15.sp,
                    color = if (isSelected) Color.Black else CalanquesGrey,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            Icon(
                painterResource(id = iconRes),
                null,
                tint = if (isSelected) CalanquesBlue else Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}