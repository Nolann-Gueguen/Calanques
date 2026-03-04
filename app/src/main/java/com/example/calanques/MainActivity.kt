package com.example.calanques


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.calanques.ui.theme.CalanquesTheme



// N'oublie pas d'importer HomeScreen si Android Studio le demande !
// import com.example.calanques.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalanquesTheme {
                // On appelle directement notre nouvel écran ici
                MainScreen()
            }
        }
    }
}