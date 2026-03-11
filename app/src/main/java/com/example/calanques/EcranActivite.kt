package com.example.calanques

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class EcranActivite : AppCompatActivity() {

    private lateinit var monRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ecran)

        monRecyclerView = findViewById(R.id.recyclerActivites)
        monRecyclerView.layoutManager = LinearLayoutManager(this)

        chargerDonnees()
    }

    private fun chargerDonnees() {
        lifecycleScope.launch {
            try {
                val resultat = RetrofitClient.instance.getActivites()
                val listeTriee = resultat.sortedBy { it.nom }
                afficherLaListe(listeTriee)

                if (resultat.isEmpty()) {
                    Toast.makeText(this@EcranActivite, "La liste est vide sur le serveur", Toast.LENGTH_SHORT).show()
                }

                afficherLaListe(resultat)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@EcranActivite, "Erreur : ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun afficherLaListe(listeDesActivites: List<Activite>) {
        val monAdapter = ActiviteAdapter(listeDesActivites)
        monRecyclerView.adapter = monAdapter
    }
}