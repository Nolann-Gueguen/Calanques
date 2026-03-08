package com.example.calanques

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

// --- TYPOGRAPHIE ---
val CustomTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = CalanquesGrey
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanierScreen() {
    // Utilisation de mutableStateListOf pour permettre la suppression dynamique
    val activites = remember {
        mutableStateListOf(
            ActivitePanier("Belvédère d'en Vau", "12/07/2025 - 15h00", 2, 25),
            ActivitePanier("Sugiton", "13/07/2025 - 10h00", 1, 15),
            ActivitePanier("Kayak", "15/07/2025 - 09h00", 3, 20)
        )
    }

    // Le total se recalcule automatiquement à chaque modification de la liste
    val total = activites.sumOf { it.montant }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mon Panier",
                        style = CustomTypography.titleLarge.copy(color = Color.White)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CalanquesBlue
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 15.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total à régler", style = CustomTypography.bodyMedium)
                        Text(
                            "$total €",
                            style = CustomTypography.titleLarge.copy(fontSize = 26.sp),
                            color = CalanquesBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Action de réservation */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CalanquesBlue)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.basket_bold),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("RÉSERVER", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (activites.isEmpty()) {
            // Affichage si le panier est vide
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Votre panier est vide", style = CustomTypography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(CalanquesLightGrey),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(activites) { activite ->
                    ItemPanier(
                        activite = activite,
                        onDelete = { activites.remove(activite) }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemPanier(activite: ActivitePanier, onDelete: () -> Unit) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Titre de l'activité en Bleu Calanques
                Text(
                    text = activite.titre,
                    style = CustomTypography.titleLarge,
                    color = CalanquesBlue,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ligne Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.calendar_blank_bold),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CalanquesGrey
                    )
                    Text("  ${activite.date}", style = CustomTypography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ligne Participants et Prix
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.user_bold),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = CalanquesBlue
                        )
                        Text(
                            "  ${activite.personnes} pers.",
                            style = CustomTypography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                    Text(
                        "${activite.montant}€",
                        style = CustomTypography.titleLarge.copy(fontSize = 22.sp),
                        color = Color.Black
                    )
                }
            }

            // Bouton de suppression Rouge Calanques
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.trash_bold),
                    contentDescription = "Supprimer",
                    tint = CalanquesRed,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}