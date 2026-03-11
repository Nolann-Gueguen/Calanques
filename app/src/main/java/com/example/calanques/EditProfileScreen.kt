package com.example.calanques

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calanques.ui.theme.CalanquesBlue
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    user: UserResponse,
    token: String,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    // On initialise les champs avec les données actuelles de l'utilisateur
    var nom by remember { mutableStateOf(user.nom ?: "") }
    var prenom by remember { mutableStateOf(user.prenom ?: "") }
    var email by remember { mutableStateOf(user.email) }
    var adresse by remember { mutableStateOf(user.adresse ?: "") }
    var cp by remember { mutableStateOf(user.cp ?: "") }
    var ville by remember { mutableStateOf(user.ville ?: "") }
    var telephone by remember { mutableStateOf(user.telephone ?: "") }

    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Modifier mon profil",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = CalanquesBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Réutilisation du style OutlinedTextField pour la DA
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CalanquesBlue,
            focusedLabelColor = CalanquesBlue
        )

        OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prénom") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors, singleLine = true)
        OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors, singleLine = true)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors, singleLine = true)
        OutlinedTextField(value = adresse, onValueChange = { adresse = it }, label = { Text("Adresse") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors, singleLine = true)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = cp, onValueChange = { cp = it }, label = { Text("CP") }, modifier = Modifier.weight(1f), colors = fieldColors, singleLine = true)
            OutlinedTextField(value = ville, onValueChange = { ville = it }, label = { Text("Ville") }, modifier = Modifier.weight(1.5f), colors = fieldColors, singleLine = true)
        }

        OutlinedTextField(value = telephone, onValueChange = { telephone = it }, label = { Text("Téléphone") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp), colors = fieldColors, singleLine = true)

        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val request = UserUpdateRequest(
                            nom = nom,
                            prenom = prenom,
                            email = email,
                            adresse = adresse,
                            cp = cp,
                            ville = ville,
                            telephone = telephone
                        )
                        // Appel à la route PUT de ton API
                        RetrofitClient.instance.updateMe("Bearer $token", request)
                        isLoading = false
                        onSaveSuccess() // On retourne au profil mis à jour
                    } catch (e: Exception) {
                        isLoading = false
                        // On pourrait ajouter un log ici pour déboguer si ça échoue
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CalanquesBlue),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Enregistrer les modifications", color = Color.White)
            }
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
        ) {
            Text("Annuler", color = Color.Gray)
        }
    }
}