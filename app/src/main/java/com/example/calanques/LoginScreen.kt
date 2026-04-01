package com.example.calanques

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calanques.ui.theme.CalanquesBlue
import kotlinx.coroutines.launch
import retrofit2.HttpException

// --- 1. ÉCRAN DE CONNEXION (MIS À JOUR AVEC GESTION DES RÔLES) ---
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 32.dp),
            contentScale = ContentScale.Fit
        )

        Text("Se connecter", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CalanquesBlue, modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 16.dp))
        }

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CalanquesBlue, focusedLabelColor = CalanquesBlue)
        )

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CalanquesBlue, focusedLabelColor = CalanquesBlue)
        )

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Champs vides !"
                } else {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            // 1. Connexion pour obtenir le Token
                            val response = RetrofitClient.instance.login(username = email, password = password)
                            val token = "${response.token_type} ${response.access_token}"

                            // 2. Récupération du profil pour avoir le role_id
                            val userProfile = RetrofitClient.instance.getMe(token)

                            // 3. Sauvegarde dans le SessionManager (Token + Rôle)
                            sessionManager.saveAuthToken(token)
                            sessionManager.saveUserRole(userProfile.role_id)

                            isLoading = false
                            Log.d("API_DEBUG", "Connexion réussie. Rôle: ${userProfile.role_id}")

                            onLoginSuccess(token)
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Identifiants incorrects ou serveur éteint."
                            Log.e("API_DEBUG", "Login Error", e)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CalanquesBlue),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Valider", color = Color.White)
        }

        TextButton(onClick = { onNavigateToSignUp() }) {
            Text("Pas de compte ? Créez-en un !", color = CalanquesBlue, fontWeight = FontWeight.Bold)
        }
    }
}

// --- 2. ÉCRAN DE CRÉATION DE COMPTE ---
@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    var prenom by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var cp by remember { mutableStateOf("") }
    var ville by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 16.dp))
        Text("Créer un compte", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CalanquesBlue, modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 16.dp))
        }

        val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CalanquesBlue, focusedLabelColor = CalanquesBlue)

        OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prénom") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors)
        OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors)
        OutlinedTextField(value = telephone, onValueChange = { telephone = it }, label = { Text("Téléphone") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors)
        OutlinedTextField(value = adresse, onValueChange = { adresse = it }, label = { Text("Adresse") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = fieldColors)

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = cp, onValueChange = { cp = it }, label = { Text("CP") }, modifier = Modifier.weight(1f), colors = fieldColors)
            OutlinedTextField(value = ville, onValueChange = { ville = it }, label = { Text("Ville") }, modifier = Modifier.weight(1.5f), colors = fieldColors)
        }

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mot de passe") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), colors = fieldColors)

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Veuillez remplir les champs."
                } else {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val request = UserCreateRequest(nom=nom, prenom=prenom, email=email, password=password, adresse=adresse, cp=cp, ville=ville, telephone=telephone, role_id=1)
                            RetrofitClient.instance.registerUser(request)
                            isLoading = false
                            onSignUpSuccess()
                        } catch (e: HttpException) {
                            isLoading = false
                            val error = e.response()?.errorBody()?.string()
                            errorMessage = if (error?.contains("Email") == true) "Email déjà utilisé" else "Erreur serveur"
                            Log.e("API_DEBUG", "Code ${e.code()} : $error")
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Erreur réseau"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CalanquesBlue),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("S'inscrire", color = Color.White)
        }

        TextButton(onClick = { onNavigateToLogin() }) {
            Text("Déjà un compte ? Connectez-vous !", color = CalanquesBlue)
        }
    }
}