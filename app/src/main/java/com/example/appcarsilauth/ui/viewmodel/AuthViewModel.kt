package com.example.appcarsilauth.ui.viewmodel

import androidx.compose.runtime.State
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

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    var email by mutableStateOf("")
    var pin by mutableStateOf("")
    
    // Captcha State (CARSIL-POL-ACC)
    var currentCaptchaCode by mutableStateOf("")
    var userCaptchaInput by mutableStateOf("")

    fun onEmailChange(newValue: String) { email = newValue }
    fun onPinChange(newValue: String) { pin = newValue }
    fun onCaptchaInputChange(newValue: String) { userCaptchaInput = newValue }
    fun setGeneratedCaptcha(code: String) { currentCaptchaCode = code }

    // Variable to track human vs bot (timestamp logic)
    private var lastAttemptTime: Long = 0

    fun checkInitialLockout(userId: String) {
        viewModelScope.launch {
            if (validateLockoutUseCase.isLockedOut(userId)) {
                startLockoutTimer(userId)
            }
        }
    }

    fun attemptLogin(email: String, pin: String) {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastAttemptTime < 500) {
            _authState.value = AuthState.Error("Actividad inusual detectada. Intento bloqueado por seguridad.")
            registerAuditLog(email, "LOGIN_BLOCKED_BOT", "AUTH_MODULE")
            return
        }
        lastAttemptTime = currentTime

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val trimmedEmail = email.trim()
            val trimmedPin = pin.trim()

            if (validateLockoutUseCase.isLockedOut(trimmedEmail)) {
                startLockoutTimer(trimmedEmail)
                return@launch
            }

            try {
                // Captcha Validation (CARSIL-POL-ACC)
                if (userCaptchaInput.uppercase() != currentCaptchaCode.uppercase()) {
                    _authState.value = AuthState.Error("Código de verificación incorrecto.")
                    registerAuditLog(trimmedEmail, "LOGIN_FAILED_CAPTCHA", "AUTH_MODULE")
                    return@launch
                }

                // 🚨 BYPASS GLOBAL DE EMERGENCIA PARA DEMO (CARSIL-BYPASS-TOTAL)
                if (trimmedEmail == "csilva@carsil.com" && trimmedPin == "carsil2024") {
                    val jwtToken = JwtTokenManager.generateToken(trimmedEmail, 1, 1)
                    _authState.value = AuthState.Success(tokenJwt = jwtToken, email = trimmedEmail, roleId = 1)
                    registerAuditLog(trimmedEmail, "LOGIN_SUCCESS_DEMO_MASTER", "AUTH_MODULE")
                    return@launch
                }

                // Local Intranet Validation (Standard)
                val usuario = intranetDao.getUserByEmail(trimmedEmail)
                
                if (usuario != null && BCrypt.checkpw(trimmedPin, usuario.Clave)) {
                    val jwtToken = JwtTokenManager.generateToken(trimmedEmail, usuario.IdRol, usuario.IdUsuario)
                    _authState.value = AuthState.Success(tokenJwt = jwtToken, email = trimmedEmail, roleId = usuario.IdRol)
                    registerAuditLog(trimmedEmail, "LOGIN_SUCCESS_JWT_ISSUED", "AUTH_MODULE")
                } else {
                    val isLocked = validateLockoutUseCase.recordFailedLogin(email)
                    registerAuditLog(email, "LOGIN_FAILED", "AUTH_MODULE")
                    
                    if (isLocked) {
                        startLockoutTimer(email)
                    } else {
                        _authState.value = AuthState.Error("Credenciales incorrectas locales.")
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
