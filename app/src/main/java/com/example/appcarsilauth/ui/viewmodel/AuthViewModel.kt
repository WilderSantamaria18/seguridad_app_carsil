package com.example.appcarsilauth.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarsilauth.domain.use_case.ValidateLockoutUseCase
import com.example.appcarsilauth.data.local.dao.AuditDao
import com.example.appcarsilauth.data.local.dao.IntranetDao
import com.example.appcarsilauth.data.local.entity.AuditLog
import com.example.appcarsilauth.util.JwtTokenManager
import org.mindrot.jbcrypt.BCrypt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LockedOut(val remainingTimeMs: Long) : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val tokenJwt: String, val email: String, val roleId: Int) : AuthState()
}

class AuthViewModel(
    private val validateLockoutUseCase: ValidateLockoutUseCase,
    private val auditDao: AuditDao,
    private val intranetDao: IntranetDao
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    var email by mutableStateOf("")
    var pin by mutableStateOf("")
    
    // Captcha State (CARSIL-POL-ACC)
    var currentCaptchaCode by mutableStateOf("")
    var userCaptchaInput by mutableStateOf("")
    var isCaptchaVerified by mutableStateOf(false)

    fun onEmailChange(newValue: String) { email = newValue.trim().lowercase(Locale.ROOT) }
    fun onPinChange(newValue: String) { pin = newValue }
    fun onCaptchaInputChange(newValue: String) {
        userCaptchaInput = newValue
        isCaptchaVerified = false
    }

    fun setGeneratedCaptcha(code: String) {
        currentCaptchaCode = code
        isCaptchaVerified = false
    }

    fun verifyCaptchaBeforeLogin(): Boolean {
        val isValid = userCaptchaInput.trim().uppercase() == currentCaptchaCode.trim().uppercase()
        isCaptchaVerified = isValid

        if (!isValid) {
            _authState.value = AuthState.Error("Verificacion anti-bot invalida. Pulsa 'No soy un robot' correctamente.")
        }

        return isValid
    }

    // Variable to track human vs bot (timestamp logic)
    private var lastAttemptTime: Long = 0

    fun checkInitialLockout(userId: String) {
        viewModelScope.launch {
            val normalizedUserId = userId.trim().lowercase(Locale.ROOT)
            if (validateLockoutUseCase.isLockedOut(normalizedUserId)) {
                startLockoutTimer(normalizedUserId)
            }
        }
    }

    fun attemptLogin(email: String, pin: String) {
        val currentTime = System.currentTimeMillis()
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val trimmedPin = pin.trim()

        if (normalizedEmail.isBlank() || trimmedPin.isBlank()) {
            _authState.value = AuthState.Error("Completa correo y contraseña para continuar.")
            return
        }

        if (!isCaptchaVerified) {
            _authState.value = AuthState.Error("Primero debes pulsar 'No soy un robot' antes de iniciar sesion.")
            registerAuditLog(normalizedEmail, "LOGIN_BLOCKED_CAPTCHA_NOT_VERIFIED", "AUTH_MODULE")
            return
        }

        // Obliga la verificacion captcha por cada intento de login.
        isCaptchaVerified = false
        
        if (currentTime - lastAttemptTime < 500) {
            _authState.value = AuthState.Error("Actividad inusual detectada. Intento bloqueado por seguridad.")
            registerAuditLog(normalizedEmail, "LOGIN_BLOCKED_BOT", "AUTH_MODULE")
            return
        }
        lastAttemptTime = currentTime

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (validateLockoutUseCase.isLockedOut(normalizedEmail)) {
                startLockoutTimer(normalizedEmail)
                return@launch
            }

            try {
                // Captcha Validation (CARSIL-POL-ACC)
                if (userCaptchaInput.trim().uppercase() != currentCaptchaCode.trim().uppercase()) {
                    _authState.value = AuthState.Error("Código de verificación incorrecto.")
                    registerAuditLog(normalizedEmail, "LOGIN_FAILED_CAPTCHA", "AUTH_MODULE")
                    return@launch
                }

                // Local Intranet Validation (Standard)
                val usuario = intranetDao.getUserByEmail(normalizedEmail)
                
                if (usuario != null && BCrypt.checkpw(trimmedPin, usuario.Clave)) {
                    validateLockoutUseCase.onSuccessfulLogin(normalizedEmail)
                    val jwtToken = JwtTokenManager.generateToken(usuario.Correo.lowercase(Locale.ROOT), usuario.IdRol, usuario.IdUsuario)
                    _authState.value = AuthState.Success(tokenJwt = jwtToken, email = usuario.Correo.lowercase(Locale.ROOT), roleId = usuario.IdRol)
                    registerAuditLog(normalizedEmail, "LOGIN_SUCCESS_JWT_ISSUED", "AUTH_MODULE")
                } else {
                    val isLocked = validateLockoutUseCase.recordFailedLogin(normalizedEmail)
                    registerAuditLog(normalizedEmail, "LOGIN_FAILED", "AUTH_MODULE")
                    
                    if (isLocked) {
                        startLockoutTimer(normalizedEmail)
                    } else {
                        _authState.value = AuthState.Error("Credenciales incorrectas. Verifica correo y contraseña.")
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error validando datos: ${e.localizedMessage}")
            }
        }
    }

    private fun registerAuditLog(userId: String, action: String, target: String) {
        viewModelScope.launch {
            auditDao.insertLog(
                AuditLog(
                    userId = userId,
                    action = action,
                    targetTable = target,
                    timestamp = System.currentTimeMillis(),
                    deviceMetadata = "Android Platform v.Auth"
                )
            )
        }
    }

    private fun startLockoutTimer(userId: String) {
        viewModelScope.launch {
            var remaining = validateLockoutUseCase.getRemainingLockoutTimeMs(userId)
            while (remaining > 0) {
                _authState.value = AuthState.LockedOut(remaining)
                delay(1000)
                remaining -= 1000
            }
            _authState.value = AuthState.Idle
        }
    }
}
