package com.example.calanques


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.calanques.ui.theme.CalanquesTheme



// N'oublie pas d'importer HomeScreen si Android Studio le demande !
// import com.example.calanques.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            // Remplace "MonTheme" par le nom du thème de ton projet (ex: CalanquesTheme)
            CalanquesTheme {
                // C'est ICI qu'il faut appeler ton composable !
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PanierScreen()
                }
                // On appelle directement notre nouvel écran ici
                MainScreen()
            }
        }
    }
}