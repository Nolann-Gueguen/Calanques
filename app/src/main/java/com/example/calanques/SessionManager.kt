package com.example.calanques

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "auth_token"
        private const val USER_ROLE = "user_role" // Nouvelle clé pour le rôle
    }

    /**
     * Sauvegarde le token JWT
     */
    fun saveAuthToken(token: String) {
        prefs.edit().putString(USER_TOKEN, token).apply()
    }

    /**
     * Sauvegarde le rôle (1 pour Client, 2 pour Admin)
     */
    fun saveUserRole(roleId: Int) {
        prefs.edit().putInt(USER_ROLE, roleId).apply()
    }

    /**
     * Récupère le token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Récupère le rôle (par défaut 1 = Client)
     */
    fun getUserRole(): Int {
        return prefs.getInt(USER_ROLE, 1)
    }

    /**
     * Déconnexion complète
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}