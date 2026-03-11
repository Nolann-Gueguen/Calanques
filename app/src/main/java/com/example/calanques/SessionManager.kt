package com.example.calanques

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    // Création du fichier de préférences nommé "user_session"
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "auth_token"
    }

    /**
     * Sauvegarde le token JWT après une connexion réussie
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply() // Sauvegarde en arrière-plan
    }

    /**
     * Récupère le token pour savoir si l'utilisateur est toujours connecté
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Efface le token (pour la déconnexion)
     */
    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.apply()
    }
}