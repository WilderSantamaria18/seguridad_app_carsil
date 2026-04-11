package com.example.appcarsilauth.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de preferencias para el login de CARSIL.
 * Almacena el correo electrónico si el usuario marca "Recuérdame".
 */
class LoginPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("carsil_login_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REMEMBERED_EMAIL = "remembered_email"
    }

    /** Guarda el correo electrónico en el almacenamiento local */
    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_REMEMBERED_EMAIL, email).apply()
    }

    /** Recupera el correo guardado, si existe */
    fun getEmail(): String? {
        return prefs.getString(KEY_REMEMBERED_EMAIL, null)
    }

    /** Limpia el correo guardado */
    fun clearEmail() {
        prefs.edit().remove(KEY_REMEMBERED_EMAIL).apply()
    }
}
