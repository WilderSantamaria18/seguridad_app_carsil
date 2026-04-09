package com.example.appcarsilauth.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val activity: FragmentActivity) {

    fun checkDeviceHasBiometric(onResult: (Boolean, String) -> Unit) {
        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> onResult(true, "Disponible")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> onResult(false, "No hay hardware biométrico")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> onResult(false, "Hardware no disponible")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> onResult(false, "No hay huellas registradas")
            else -> onResult(false, "Error desconocido")
        }
    }

    fun showBiometricPrompt(
        title: String = "Autenticación Biométrica",
        subtitle: String = "Usa tu huella para entrar a CARSIL",
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (Int, CharSequence) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Usar PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
