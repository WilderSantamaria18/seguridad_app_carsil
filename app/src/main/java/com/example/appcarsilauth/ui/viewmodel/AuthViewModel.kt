package com.example.appcarsilauth.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarsilauth.data.local.dao.AuditDao
import com.example.appcarsilauth.data.local.dao.IntranetDao
import com.example.appcarsilauth.data.local.entity.AuditLog
import com.example.appcarsilauth.data.remote.RailwayDatabase
import com.example.appcarsilauth.domain.use_case.ValidateLockoutUseCase
import com.example.appcarsilauth.util.JwtTokenManager
import com.example.appcarsilauth.util.LoginPreferences
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object InactiveUser : AuthState()                         // Cuenta desactivada por el admin
    data class LockedOut(val remainingTimeMs: Long) : AuthState()
    data class Error(val message: String) : AuthState()
    object SuccessAnimation : AuthState()
    data class Success(val tokenJwt: String, val email: String, val roleId: Int, val userName: String) : AuthState()
}

class AuthViewModel(
    private val validateLockoutUseCase: ValidateLockoutUseCase,
    private val auditDao: AuditDao,
    private val intranetDao: IntranetDao,
    private val loginPreferences: LoginPreferences
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

    init {
        // Cargar correo recordado al iniciar
        val savedEmail = loginPreferences.getEmail()
        if (savedEmail != null) {
            email = savedEmail
            rememberMe = true
        }
    }

    // Flujo de pasos (BBVA Style)
    var loginStep by mutableStateOf(1) // 1: Email, 2: Pass/Biometry
    var identifiedUserName by mutableStateOf("")
    var identifiedUserId by mutableStateOf(-1)
    var identifiedRoleId by mutableStateOf(-1)
    private var identifiedUserHash: String? = null // Guardamos el hash temporalmente

    fun onEmailChange(newValue: String) {
        email = newValue.trim().lowercase(Locale.ROOT)
    }
    fun onPinChange(newValue: String) {
        pin = newValue
    }
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
        // NO limpiamos email ni rememberMe aquí para que persistan si hubo éxito
        pin = ""
        userCaptchaInput = ""
        captchaAttemptsInModal = 0
        isCaptchaVerified = false
        loginStep = 1
        // identifiedUserName se limpia en logout, no aquí para no afectar el Dashboard inicial
    }

    private fun handleRememberMePersistence(emailToSave: String) {
        if (rememberMe) {
            loginPreferences.saveEmail(emailToSave)
        } else {
            loginPreferences.clearEmail()
        }
    }

    fun backToStep1() {
        loginStep = 1
        pin = ""
    }

    // PASO 1: Identificar Usuario por Email
    fun identifyUser() {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        if (normalizedEmail.isBlank()) {
            _authState.value = AuthState.Error("Ingresa tu correo.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Buscar usuario ACTIVO en Railway (Estado = 1)
                val remoteUser = RailwayDatabase.getUserByEmail(normalizedEmail)

                if (remoteUser != null) {
                    identifiedUserName = "${remoteUser["Nombres"]} ${remoteUser["Apellidos"]}"
                    identifiedUserId = remoteUser["IdUsuario"] as Int
                    identifiedRoleId = if (remoteUser["Rol"] == "Administrador") 1 else 2
                    identifiedUserHash = remoteUser["Clave"] as String
                    _authState.value = AuthState.Idle
                    loginStep = 2
                } else {
                    // Usuario no activo → verificar si existe con cualquier estado
                    val anyUser = RailwayDatabase.getUserByEmailAnyStatus(normalizedEmail)

                    when {
                        anyUser != null && (anyUser["Estado"] as? Int ?: 1) == 0 -> {
                            // CUENTA INACTIVA: existe pero está desactivada por el administrador
                            registerAuditLog(normalizedEmail, "LOGIN_DENIED_INACTIVE", "AUTH_MODULE")
                            _authState.value = AuthState.InactiveUser
                        }
                        anyUser == null -> {
                            // No existe en nube → intentar base local
                            val usuario = intranetDao.getUserByEmail(normalizedEmail)
                            if (usuario != null) {
                                identifiedUserName = "${usuario.Nombres} ${usuario.Apellidos}"
                                identifiedUserId = usuario.IdUsuario
                                identifiedRoleId = usuario.IdRol
                                _authState.value = AuthState.Idle
                                loginStep = 2
                            } else {
                                _authState.value = AuthState.Error("Usuario no registrado en CARSIL.")
                            }
                        }
                        else -> {
                            _authState.value = AuthState.Error("Usuario no registrado en CARSIL.")
                        }
                    }
                }
            } catch (e: Throwable) {
                _authState.value = AuthState.Error("Conexión Railway: ${e.message}")
            }
        }
    }

    fun checkInitialLockout(userId: String) {
        viewModelScope.launch {
            val normalizedUserId = userId.trim().lowercase(Locale.ROOT)
            if (validateLockoutUseCase.isLockedOut(normalizedUserId)) {
                startLockoutTimer(normalizedUserId)
            }
        }
    }

    fun preVerifyLogin() {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val trimmedPin = pin.trim()

        if (normalizedEmail.isBlank() || trimmedPin.isBlank()) {
            _authState.value = AuthState.Error("Completa correo y contraseña para continuar.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Verificar bloqueo ANTES de intentar la contraseña
            if (validateLockoutUseCase.isLockedOut(normalizedEmail)) {
                startLockoutTimer(normalizedEmail)
                return@launch
            }

            try {
                val storedHash = (identifiedUserHash ?: intranetDao.getUserByEmail(normalizedEmail)?.Clave)?.replaceFirst("${'$'}2b${'$'}", "${'$'}2a${'$'}")

                if (storedHash != null && BCrypt.checkpw(trimmedPin, storedHash)) {
                    // Contraseña correcta → resetear intentos y abrir captcha
                    validateLockoutUseCase.onSuccessfulLogin(normalizedEmail)
                    _authState.value = AuthState.Idle
                    isCaptchaModalVisible = true
                    captchaAttemptsInModal = 0
                } else {
                    // Contraseña incorrecta → registrar fallo y calcular intentos restantes
                    val isNowLocked = validateLockoutUseCase.recordFailedLogin(normalizedEmail)
                    pin = ""
                    registerAuditLog(normalizedEmail, "LOGIN_FAILED", "AUTH_MODULE")

                    if (isNowLocked) {
                        // Se alcanzó el límite: bloquear 5 minutos
                        startLockoutTimer(normalizedEmail)
                    } else {
                        // Calcular intentos usados para mostrar mensaje informativo
                        val remaining = validateLockoutUseCase.getRemainingAttempts(normalizedEmail)
                        val used = ValidateLockoutUseCase.MAX_ATTEMPTS - remaining
                        _authState.value = AuthState.Error(
                            "Contraseña incorrecta. Intento $used de ${ValidateLockoutUseCase.MAX_ATTEMPTS}. " +
                            "Te quedan $remaining intento${if (remaining == 1) "" else "s"} antes del bloqueo."
                        )
                    }
                }
            } catch (e: Throwable) {
                _authState.value = AuthState.Error("Error de validación: ${e.message}")
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
                    val remoteUser = RailwayDatabase.getUserByEmail(normalizedEmail)

                    if (remoteUser != null) {
                        validateLockoutUseCase.onSuccessfulLogin(normalizedEmail)
                        val jwtToken =
                                JwtTokenManager.generateToken(
                                        remoteUser["Correo"] as String,
                                        if (remoteUser["Rol"] == "Administrador") 1 else 2,
                                        remoteUser["IdUsuario"] as Int
                                )

                        val finalEmail = remoteUser["Correo"] as? String ?: normalizedEmail
                        val finalRole = if (remoteUser["Rol"] == "Administrador") 1 else 2
                        val finalName = identifiedUserName 

                        isCaptchaModalVisible = false
                        _authState.value = AuthState.SuccessAnimation
                        registerAuditLog(normalizedEmail, "LOGIN_SUCCESS_REMOTE", "AUTH_MODULE")
                        
                        handleRememberMePersistence(finalEmail)
                        clearInputs()

                        delay(4000)
                        _authState.value = AuthState.Success(
                            tokenJwt = jwtToken,
                            email = finalEmail,
                            roleId = finalRole,
                            userName = finalName
                        )
                    } else {
                        // Respaldo local si no está en nube en este paso crítico
                        val usuario = intranetDao.getUserByEmail(normalizedEmail)
                        if (usuario != null) {
                            validateLockoutUseCase.onSuccessfulLogin(normalizedEmail)
                            val jwtToken = JwtTokenManager.generateToken(
                                usuario.Correo,
                                usuario.IdRol,
                                usuario.IdUsuario
                            )
                            
                            isCaptchaModalVisible = false
                            _authState.value = AuthState.SuccessAnimation
                            
                            handleRememberMePersistence(usuario.Correo)
                            val nameToPass = "${usuario.Nombres} ${usuario.Apellidos}"
                            clearInputs()

                            delay(4000)
                            _authState.value = AuthState.Success(
                                tokenJwt = jwtToken,
                                email = usuario.Correo,
                                roleId = usuario.IdRol,
                                userName = nameToPass
                            )
                        } else {
                            _authState.value = AuthState.Error("Usuario no encontrado localmente.")
                        }
                    }

                } catch (e: Exception) {
                    _authState.value = AuthState.Error("Error finalizando sesión remota.")
                }
            }
        } else {
            // Captcha Incorrecto
            captchaAttemptsInModal++
            if (captchaAttemptsInModal >= 5) {
                isCaptchaModalVisible = false
                _authState.value =
                        AuthState.Error("Demasiados intentos de captcha. Proceso reiniciado.")
                clearInputs() // Seguridad: Borrar todo si exceden intentos
                registerAuditLog(normalizedEmail, "CAPTCHA_ATTEMPTS_EXCEEDED", "AUTH_MODULE")
            } else {
                userCaptchaInput = ""
                _authState.value =
                        AuthState.Error("Código incorrecto. Intento $captchaAttemptsInModal de 5.")
                // Generar nuevo captcha automáticamente? O dejar que use el botón de refrescar
            }
        }
    }

    fun handleBiometricSuccess(storedEmail: String?) {
        val targetEmail = storedEmail ?: email
        if (targetEmail.isNotBlank()) {
            loginByEmail(targetEmail)
        } else {
            _authState.value =
                    AuthState.Error("No hay usuario enlazado. Inicia sesión normalmente primero.")
        }
    }

    private fun loginByEmail(targetEmail: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // BIOMETRÍA TAMBIÉN BUSCA EN RAILWAY
                val remoteUser =
                        RailwayDatabase.getUserByEmail(targetEmail)
                                ?: mapOf(
                                        "Correo" to
                                                (intranetDao.getUserByEmail(targetEmail)?.Correo
                                                        ?: ""),
                                        "IdRol" to
                                                (intranetDao.getUserByEmail(targetEmail)?.IdRol
                                                        ?: 2),
                                        "IdUsuario" to
                                                (intranetDao.getUserByEmail(targetEmail)?.IdUsuario
                                                        ?: 0),
                                        "Rol" to "Empleado"
                                )

                val finalEmail = remoteUser["Correo"] as String
                val finalRole =
                        if (remoteUser["Rol"] == "Administrador") 1
                        else (remoteUser["IdRol"] as? Int ?: 2)
                val finalId = remoteUser["IdUsuario"] as Int

                if (finalEmail.isNotEmpty()) {
                    validateLockoutUseCase.onSuccessfulLogin(targetEmail)
                    val jwtToken = JwtTokenManager.generateToken(finalEmail, finalRole, finalId)
                    _authState.value = AuthState.SuccessAnimation
                    registerAuditLog(finalEmail, "LOGIN_BIOMETRIC_SUCCESS", "AUTH_MODULE")
                    
                    handleRememberMePersistence(finalEmail)
                    // Capturar nombre antes de un posible clear futuro (aunque biometría no suele llamar clearInputs)
                    val nameToPass = identifiedUserName.ifEmpty { finalEmail }
                    
                    delay(4000)
                    _authState.value =
                            AuthState.Success(
                                    tokenJwt = jwtToken,
                                    email = finalEmail,
                                    roleId = finalRole,
                                    userName = nameToPass
                            )
                } else {
                    _authState.value = AuthState.Error("Falla crítica de identidad.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al validar biometría remota.")
            }
        }
    }

    fun refreshToken() {
        val currentState = _authState.value
        if (currentState is AuthState.Success) {
            viewModelScope.launch {
                val newToken =
                        JwtTokenManager.generateToken(
                                currentState.email,
                                currentState.roleId,
                                1 /* userId simulado o extraido */
                        )
                _authState.value = currentState.copy(tokenJwt = newToken, userName = currentState.userName)
                registerAuditLog(currentState.email, "TOKEN_REFRESH", "AUTH_MODULE")
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

    fun changePassword(userId: Int, currentPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val emailToSearch = email.ifBlank { identifiedUserName.split(" ").lastOrNull() ?: "" } // Intento de obtener context
                // Si no tenemos el email a la mano, buscamos por ID primero o usamos el email guardado
                val user = RailwayDatabase.getUserByEmail(email) 
                
                val storedHash = (user?.get("Clave") as? String)?.replaceFirst("$2b$", "$2a$")
                
                if (storedHash != null && BCrypt.checkpw(currentPass, storedHash)) {
                    val newHash = BCrypt.hashpw(newPass, BCrypt.gensalt())
                    val success = RailwayDatabase.updateUserPassword(userId, newHash)
                    if (success) {
                        onResult(true, "Contraseña actualizada correctamente.")
                        registerAuditLog(email, "PASSWORD_CHANGED", "USUARIO")
                    } else {
                        onResult(false, "Error al actualizar en la base de datos.")
                    }
                } else {
                    onResult(false, "La contraseña actual es incorrecta.")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            } finally {
                _authState.value = AuthState.Idle
            }
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
