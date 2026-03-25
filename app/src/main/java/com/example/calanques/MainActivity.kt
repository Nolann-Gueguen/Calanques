package com.example.calanques

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.calanques.ui.theme.CalanquesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalanquesTheme {
                AppNavigation()
            }
        }
    }
}

// --- POINT D'ENTRÉE DE LA NAVIGATION ---
@Composable
fun AppNavigation() {
    // true = on est sur le splash, false = on est dans l'app
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onEnter = { showSplash = false })
    } else {
        MainScreen()
    }
}