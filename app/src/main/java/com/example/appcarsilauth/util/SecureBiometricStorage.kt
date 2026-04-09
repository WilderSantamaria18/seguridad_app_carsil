package com.example.appcarsilauth.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Almacenamiento seguro para datos biométricos.
 * Usa EncryptedSharedPreferences con una MasterKey protegida por el KeyStore de Android.
 * Los datos solo pueden ser descifrados en ESTE dispositivo.
 */
class SecureBiometricStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "carsil_biometric_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LINKED_EMAIL = "linked_email"
        private const val KEY_LINKED_USER_ID = "linked_user_id"
        private const val KEY_LINKED_ROLE_ID = "linked_role_id"
        private const val KEY_LINKED_USER_NAME = "linked_user_name"
    }

    /** Registra (enlaza) un usuario con la biometría del dispositivo */
    fun enrollUser(email: String, userId: Int, roleId: Int, userName: String) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, true)
            .putString(KEY_LINKED_EMAIL, email)
            .putInt(KEY_LINKED_USER_ID, userId)
            .putInt(KEY_LINKED_ROLE_ID, roleId)
            .putString(KEY_LINKED_USER_NAME, userName)
            .apply()
    }

    /** Elimina el enlace biométrico */
    fun removeEnrollment() {
        prefs.edit().clear().apply()
    }

    /** Verifica si hay un usuario enlazado a la biometría */
    fun isBiometricEnrolled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /** Verifica si un usuario ESPECÍFICO es el que tiene la huella enlazada */
    fun isEnrolledFor(email: String): Boolean {
        return isBiometricEnrolled() && getLinkedEmail()?.lowercase() == email.lowercase()
    }

    /** Obtiene el email del usuario enlazado */
    fun getLinkedEmail(): String? {
        return prefs.getString(KEY_LINKED_EMAIL, null)
    }

    /** Obtiene el ID del usuario enlazado */
    fun getLinkedUserId(): Int {
        return prefs.getInt(KEY_LINKED_USER_ID, -1)
    }

    /** Obtiene el rol del usuario enlazado */
    fun getLinkedRoleId(): Int {
        return prefs.getInt(KEY_LINKED_ROLE_ID, -1)
    }

    /** Obtiene el nombre del usuario enlazado */
    fun getLinkedUserName(): String? {
        return prefs.getString(KEY_LINKED_USER_NAME, null)
    }
}
