package com.example.appcarsilauth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appcarsilauth.data.local.AppDatabase
import com.example.appcarsilauth.data.repository.SecurityRepositoryImpl
import com.example.appcarsilauth.domain.use_case.ValidateLockoutUseCase
import com.example.appcarsilauth.ui.screens.*
import com.example.appcarsilauth.ui.theme.AppCarsilAuthTheme
import com.example.appcarsilauth.ui.viewmodel.AuthState
import com.example.appcarsilauth.ui.viewmodel.AuthViewModel
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel
import com.example.appcarsilauth.util.BiometricHelper
import com.example.appcarsilauth.util.JwtTokenManager
import com.example.appcarsilauth.util.SecureBiometricStorage
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        */

        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)
        val securityRepository = SecurityRepositoryImpl(database.securityDao())
        val validateLockoutUseCase = ValidateLockoutUseCase(securityRepository)
        val biometricStorage = SecureBiometricStorage(this)
        val biometricHelper = BiometricHelper(this)

        val authViewModel: AuthViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(validateLockoutUseCase, database.auditDao(), database.intranetDao()) as T
                }
            }
        }

        val intranetViewModel: IntranetViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return IntranetViewModel(database.intranetDao()) as T
                }
            }
        }

        val canUseBiometric = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

        setContent {
            val authState by authViewModel.authState.collectAsState()
            val allowedMenus by intranetViewModel.allowedMenus.collectAsState()
            var currentScreen by remember { mutableStateOf("LOGIN") }
            var userEmail by remember { mutableStateOf("") }
            var userName by remember { mutableStateOf("") }
            var userRoleId by remember { mutableStateOf(1) }
            var userId by remember { mutableStateOf(1) }

            var isBiometricEnrolled by remember { mutableStateOf(false) }
            
            // Estado para el diálogo de transferencia de huella
            var showTransferDialog by remember { mutableStateOf(false) }

            LaunchedEffect(userEmail) {
                isBiometricEnrolled = if (userEmail.isNotBlank()) {
                    biometricStorage.isEnrolledFor(userEmail)
                } else {
                    false
                }
            }

            var lastActivityTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
            var showTimeoutDialog by remember { mutableStateOf(false) }

            LaunchedEffect(authState, lastActivityTime) {
                if (authState is AuthState.Success) {
                    while (true) {
                        delay(10000)
                        val inactiveTime = System.currentTimeMillis() - lastActivityTime
                        if (inactiveTime > 2 * 60 * 1000 && !showTimeoutDialog) {
                            showTimeoutDialog = true
                        }
                    }
                }
            }

            LaunchedEffect(authState) {
                when (val state = authState) {
                    is AuthState.Success -> {
                        if (!JwtTokenManager.isTokenValid(state.tokenJwt)) {
                            currentScreen = "LOGIN"
                            return@LaunchedEffect
                        }

                        val jwtParts = state.tokenJwt.split(".")
                        if (jwtParts.size == 3) {
                            try {
                                val payload = String(
                                    android.util.Base64.decode(jwtParts[1], android.util.Base64.URL_SAFE),
                                    java.nio.charset.StandardCharsets.UTF_8
                                )
                                val json = org.json.JSONObject(payload)
                                userId = json.getInt("sub")
                                userEmail = json.getString("email")
                                userRoleId = json.getInt("roleId")

                                val usuario = database.intranetDao().getUserByEmail(userEmail)
                                userName = if (usuario != null) "${usuario.Nombres} ${usuario.Apellidos}" else userEmail

                                intranetViewModel.loadAllowedMenus(userRoleId)
                                currentScreen = if (userRoleId == 2) "ATTENDANCE" else "DASHBOARD"
                                lastActivityTime = System.currentTimeMillis()
                            } catch (e: Exception) {
                                currentScreen = "LOGIN"
                            }
                        }
                    }
                    is AuthState.Idle, is AuthState.Error, is AuthState.LockedOut -> {
                        if (currentScreen != "PROFORMA" && currentScreen != "CLIENTS" && currentScreen != "PRODUCTS" && currentScreen != "PROFILE" && currentScreen != "CHANGE_PASSWORD") {
                            currentScreen = "LOGIN"
                        }
                    }
                    else -> {}
                }
            }

            AppCarsilAuthTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                lastActivityTime = System.currentTimeMillis()
                            })
                        },
                    color = Color.White
                ) {
                    if (showTimeoutDialog) {
                        AlertDialog(
                            onDismissRequest = { },
                            title = { Text("Sesion inactiva", color = Color.Black) },
                            text = { Text("Tu sesion expirara pronto por seguridad.", color = Color.Black) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        authViewModel.refreshToken()
                                        lastActivityTime = System.currentTimeMillis()
                                        showTimeoutDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                ) { Text("Mantener sesion") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    authViewModel.logout()
                                    showTimeoutDialog = false
                                }) { Text("Cerrar ahora", color = Color.Red) }
                            },
                            containerColor = Color.White
                        )
                    }
                    
                    // DIALOGO DE ADVERTENCIA SEGURIDAD (BLOQUEO)
                    if (showTransferDialog) {
                        AlertDialog(
                            onDismissRequest = { showTransferDialog = false },
                            title = { Text("Seguridad de cuenta", color = Color.Black) },
                            text = { Text("Otra cuenta ya tiene activa la huella dactilar en este dispositivo. Por seguridad, debe desactivar la huella desde la otra cuenta para poder activarla en esta cuenta.", color = Color.Black) },
                            confirmButton = {
                                Button(
                                    onClick = { showTransferDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                ) { Text("Cerrar") }
                            },
                            containerColor = Color.White
                        )
                    }

                    when (currentScreen) {
                        "DASHBOARD" -> DashboardScreen(
                            email = userEmail,
                            roleId = userRoleId,
                            allowedMenus = allowedMenus,
                            onGoToClients = { currentScreen = "CLIENTS" },
                            onGoToProducts = { currentScreen = "PRODUCTS" },
                            onGoToProforma = { currentScreen = "PROFORMA" },
                            onGoToProfile = { currentScreen = "PROFILE" },
                            onLogout = {
                                authViewModel.logout()
                                intranetViewModel.clearSessionState()
                                currentScreen = "LOGIN"
                            }
                        )
                        "PROFILE" -> ProfileScreen(
                            email = userEmail,
                            userName = userName,
                            roleId = userRoleId,
                            isBiometricEnrolled = isBiometricEnrolled,
                            isBiometricAvailable = canUseBiometric,
                            userId = userId,
                            onGoToChangePassword = { currentScreen = "CHANGE_PASSWORD" },
                            onEnrollBiometric = {
                                // LOGICA DE VALIDACION DE HUELLA EXISTENTE
                                if (biometricStorage.isBiometricEnrolled() && !biometricStorage.isEnrolledFor(userEmail)) {
                                    showTransferDialog = true
                                } else {
                                    biometricHelper.showBiometricPrompt(
                                        title = "Registrar huella",
                                        subtitle = "Confirma tu huella para enlazar tu cuenta",
                                        onSuccess = {
                                            biometricStorage.enrollUser(userEmail, userId, userRoleId, userName)
                                            isBiometricEnrolled = true
                                            Toast.makeText(this@MainActivity, "Huella registrada exitosamente", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { _, err ->
                                            Toast.makeText(this@MainActivity, "Error: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            onRemoveBiometric = {
                                biometricStorage.removeEnrollment()
                                isBiometricEnrolled = false
                                Toast.makeText(this@MainActivity, "Huella desactivada", Toast.LENGTH_SHORT).show()
                            },
                            onBack = { currentScreen = "DASHBOARD" }
                        )
                        "CHANGE_PASSWORD" -> ChangePasswordScreen(
                            userId = userId,
                            onChangePassword = { current, new, onRes ->
                                authViewModel.changePassword(userId, current, new, onRes)
                            },
                            onBack = { currentScreen = "PROFILE" },
                            onSuccess = {
                                authViewModel.logout()
                                intranetViewModel.clearSessionState()
                                currentScreen = "LOGIN"
                                Toast.makeText(this@MainActivity, "Contraseña actualizada. Inicie sesión nuevamente.", Toast.LENGTH_LONG).show()
                            }
                        )
                        "CLIENTS" -> ClientsScreen(viewModel = intranetViewModel, onBack = { currentScreen = "DASHBOARD" })
                        "PRODUCTS" -> ProductsScreen(viewModel = intranetViewModel, onBack = { currentScreen = "DASHBOARD" })
                        "PROFORMA" -> ProformaScreen(viewModel = intranetViewModel, idUsuario = userId, onBack = { currentScreen = "DASHBOARD" })
                        "ATTENDANCE" -> AttendanceScreen(
                            email = userEmail,
                            viewModel = intranetViewModel,
                            idUsuario = userId,
                            onLogout = {
                                authViewModel.logout()
                                intranetViewModel.clearSessionState()
                                currentScreen = "LOGIN"
                            }
                        )
                        else -> {
                            val isEnrolled = if (authViewModel.email.isNotBlank()) {
                                biometricStorage.isEnrolledFor(authViewModel.email)
                            } else {
                                false
                            }

                            LoginScreen(
                                viewModel = authViewModel,
                                isBiometricEnrolledForThisUser = isEnrolled,
                                onFingerprintClick = {
                                    if (isEnrolled) {
                                        biometricHelper.showBiometricPrompt(
                                            title = "Acceso biometrico",
                                            subtitle = "Usuario identificado: ${authViewModel.identifiedUserName}",
                                            onSuccess = {
                                                authViewModel.handleBiometricSuccess(authViewModel.email)
                                            },
                                            onError = { _, err ->
                                                Toast.makeText(this@MainActivity, "Error: $err", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Biometria no activa para esta cuenta en este equipo.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}