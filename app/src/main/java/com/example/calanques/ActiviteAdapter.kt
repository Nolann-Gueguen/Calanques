package com.example.calanques

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Ajouté
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Import de Glide

class ActiviteAdapter(private val listeActivites: List<Activite>) :
    RecyclerView.Adapter<ActiviteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nom: TextView = view.findViewById(R.id.textNom)
        val tarif: TextView = view.findViewById(R.id.textTarif)
        val imageAffiche: ImageView = view.findViewById(R.id.imageActivite)
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

        val baseUrl = "http://webngo.sio.bts:8003/"
        val cleanPath = activite.image_url.removePrefix("/")
        val fullImageUrl = baseUrl + cleanPath

        Glide.with(holder.itemView.context)
            .load(fullImageUrl)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.imageAffiche)
    }

    override fun getItemCount() = listeActivites.size
}