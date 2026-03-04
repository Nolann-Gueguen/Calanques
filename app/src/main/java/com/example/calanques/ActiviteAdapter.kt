package com.example.calanques

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActiviteAdapter(private val listeActivites: List<Activite>) :
    RecyclerView.Adapter<ActiviteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom: TextView = view.findViewById(R.id.textNom)
        val tarif: TextView = view.findViewById(R.id.textTarif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activite = listeActivites[position]
        holder.nom.text = activite.nom
        holder.tarif.text = "${activite.tarif} €"
    }

    override fun getItemCount() = listeActivites.size
}