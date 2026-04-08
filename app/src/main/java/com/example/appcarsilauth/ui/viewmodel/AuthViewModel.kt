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
    object SuccessAnimation : AuthState()
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
    var rememberMe by mutableStateOf(false)
    
    // Captcha State (CARSIL-POL-ACC)
    var currentCaptchaCode by mutableStateOf("")
    var userCaptchaInput by mutableStateOf("")
    var isCaptchaVerified by mutableStateOf(false)
    var isCaptchaModalVisible by mutableStateOf(false)
    var captchaAttemptsInModal by mutableStateOf(0)

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

    fun dismissCaptchaModal() {
        isCaptchaModalVisible = false
        userCaptchaInput = ""
        captchaAttemptsInModal = 0
    }

    fun logout() {
        _authState.value = AuthState.Idle
        clearInputs()
    }

    private fun clearInputs() {
        email = ""
        pin = ""
        userCaptchaInput = ""
        captchaAttemptsInModal = 0
        isCaptchaVerified = false
    }

    fun checkInitialLockout(userId: String) {
        viewModelScope.launch {
            val normalizedUserId = userId.trim().lowercase(Locale.ROOT)
            if (validateLockoutUseCase.isLockedOut(normalizedUserId)) {
                startLockoutTimer(normalizedUserId)
            }
        }
    }

    // Paso 1: Validar credenciales
    fun preVerifyLogin() {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val trimmedPin = pin.trim()

        if (normalizedEmail.isBlank() || trimmedPin.isBlank()) {
            _authState.value = AuthState.Error("Completa correo y contraseña para continuar.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (validateLockoutUseCase.isLockedOut(normalizedEmail)) {
                startLockoutTimer(normalizedEmail)
                return@launch
            }

            try {
                val usuario = intranetDao.getUserByEmail(normalizedEmail)
                
                if (usuario != null && BCrypt.checkpw(trimmedPin, usuario.Clave)) {
                    // Credenciales correctas -> Mostrar Captcha
                    _authState.value = AuthState.Idle
                    isCaptchaModalVisible = true
                    captchaAttemptsInModal = 0
                } else {
                    val isLocked = validateLockoutUseCase.recordFailedLogin(normalizedEmail)
                    if (isLocked) {
                        startLockoutTimer(normalizedEmail)
                    } else {
                        _authState.value = AuthState.Error("Credenciales incorrectas. Verifica correo y contraseña.")
                        // Seguridad: Borrar campos si falla
                        pin = "" 
                    }
                    registerAuditLog(normalizedEmail, "LOGIN_PRE_VERIFY_FAILED", "AUTH_MODULE")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    // Paso 2: Validar Captcha y Finalizar Login
    fun finalizeLoginWithCaptcha() {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        
        if (userCaptchaInput.trim().uppercase() == currentCaptchaCode.trim().uppercase()) {
            // Captcha Correcto -> Login Final
            viewModelScope.launch {
                _authState.value = AuthState.Loading
                try {
                    val usuario = intranetDao.getUserByEmail(normalizedEmail)
                    if (usuario != null) {
                        validateLockoutUseCase.onSuccessfulLogin(normalizedEmail)
                        val jwtToken = JwtTokenManager.generateToken(usuario.Correo, usuario.IdRol, usuario.IdUsuario)
                        
                        isCaptchaModalVisible = false
                        // PASO 3: Mostrar Animacion de Exito
                        _authState.value = AuthState.SuccessAnimation
                        registerAuditLog(normalizedEmail, "LOGIN_SUCCESS_CAPTCHA_VERIFIED", "AUTH_MODULE")
                        
                        // Seguridad: Borrar inputs inmediatamente para que no queden en memoria
                        val finalEmail = usuario.Correo
                        val finalRole = usuario.IdRol
                        clearInputs()

                        delay(4000) // Duración de la animación (4 segundos)
                        
                        // PASO FINAL: Navegar al Dashboard
                        _authState.value = AuthState.Success(tokenJwt = jwtToken, email = finalEmail, roleId = finalRole)
                    }
                } catch (e: Exception) {
                    _authState.value = AuthState.Error("Error fatal: ${e.localizedMessage}")
                }
            }
        } else {
            // Captcha Incorrecto
            captchaAttemptsInModal++
            if (captchaAttemptsInModal >= 5) {
                isCaptchaModalVisible = false
                _authState.value = AuthState.Error("Demasiados intentos de captcha. Proceso reiniciado.")
                clearInputs() // Seguridad: Borrar todo si exceden intentos
                registerAuditLog(normalizedEmail, "CAPTCHA_ATTEMPTS_EXCEEDED", "AUTH_MODULE")
            } else {
                userCaptchaInput = ""
                _authState.value = AuthState.Error("Código incorrecto. Intento $captchaAttemptsInModal de 5.")
                // Generar nuevo captcha automáticamente? O dejar que use el botón de refrescar
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
