package com.example.calanques

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calanques.ui.theme.CalanquesRed

// --- 1. ÉCRAN DE CONNEXION ---
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit = {}, onNavigateToSignUp: () -> Unit = {}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo du Parc National des Calanques",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(bottom = 32.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Se connecter",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            placeholder = { Text("johndoe@email.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            placeholder = { Text("********") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true
        )

        Button(
            onClick = { onLoginSuccess() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CalanquesRed)
        ) {
            Text("Valider", fontSize = 16.sp, color = Color.White)
        }

        TextButton(
            onClick = { /* Action Mot de passe oublié */ },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Mot de passe oublié", color = Color.Gray)
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Pas de compte ?", color = Color.Gray)
            TextButton(onClick = { onNavigateToSignUp() }) {
                Text("Créez-en un !", color = CalanquesRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 2. ÉCRAN DE CRÉATION DE COMPTE ---
@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    // Variables du formulaire
    var prenom by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var cp by remember { mutableStateOf("") }
    var ville by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }

    // Gestion des erreurs
    var errorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState), // Rend l'écran défilable
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo du Parc National des Calanques",
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 16.dp, top = 16.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Créer un compte",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        // Affichage de l'erreur si elle existe
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = CalanquesRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // --- CHAMPS D'IDENTITÉ ---
        OutlinedTextField(
            value = prenom, onValueChange = { prenom = it },
            label = { Text("Prénom") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true
        )
        OutlinedTextField(
            value = nom, onValueChange = { nom = it },
            label = { Text("Nom") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true
        )

        // --- CHAMPS DE CONTACT ---
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true
        )
        OutlinedTextField(
            value = telephone, onValueChange = { telephone = it },
            label = { Text("Téléphone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true
        )

        // --- CHAMPS D'ADRESSE ---
        OutlinedTextField(
            value = adresse, onValueChange = { adresse = it },
            label = { Text("Adresse") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), singleLine = true
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = cp, onValueChange = { cp = it },
                label = { Text("Code Postal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f), singleLine = true
            )
            OutlinedTextField(
                value = ville, onValueChange = { ville = it },
                label = { Text("Ville") },
                modifier = Modifier.weight(1.5f), singleLine = true
            )
        }

        // --- SÉCURITÉ ---
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), singleLine = true
        )

        // --- BOUTON DE VALIDATION ---
        Button(
            onClick = {
                // Vérification stricte avant envoi
                if (prenom.isBlank() || nom.isBlank() || email.isBlank() || password.isBlank() ||
                    adresse.isBlank() || cp.isBlank() || ville.isBlank() || telephone.isBlank()) {
                    errorMessage = "Veuillez remplir tous les champs."
                } else {
                    errorMessage = ""
                    // Tout est bon, on valide !
                    onSignUpSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CalanquesRed)
        ) {
            Text("S'inscrire", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Retour à la connexion
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Déjà un compte ?", color = Color.Gray)
            TextButton(onClick = { onNavigateToLogin() }) {
                Text("Connectez-vous !", color = CalanquesRed, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}