package com.example.appcarsilauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appcarsilauth.ui.theme.AppCarsilAuthTheme
import com.example.appcarsilauth.ui.screens.LoginScreen
import com.example.appcarsilauth.ui.screens.DashboardScreen
import com.example.appcarsilauth.ui.screens.ProformaScreen
import com.example.appcarsilauth.ui.screens.AttendanceScreen
import com.example.appcarsilauth.ui.screens.ClientsScreen
import com.example.appcarsilauth.ui.screens.ProductsScreen
import com.example.appcarsilauth.util.JwtTokenManager
import com.example.appcarsilauth.ui.viewmodel.AuthViewModel
import com.example.appcarsilauth.ui.viewmodel.AuthState
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel
import com.example.appcarsilauth.data.local.AppDatabase
import com.example.appcarsilauth.data.repository.SecurityRepositoryImpl
import com.example.appcarsilauth.domain.use_case.ValidateLockoutUseCase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)
        val securityRepository = SecurityRepositoryImpl(database.securityDao())
        val validateLockoutUseCase = ValidateLockoutUseCase(securityRepository)
        
        val authViewModel: AuthViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(validateLockoutUseCase, database.auditDao(), database.intranetDao()) as T
                }
            }
        }

        val intranetFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return IntranetViewModel(database.intranetDao()) as T
            }
        }
        val intranetViewModel: IntranetViewModel by viewModels { intranetFactory }

        setContent {
            val authState by authViewModel.authState.collectAsState()
            val allowedMenus by intranetViewModel.allowedMenus.collectAsState()
            var currentScreen by remember { mutableStateOf("LOGIN") } // LOGIN, DASHBOARD, CLIENTS, PRODUCTS, PROFORMA, ATTENDANCE
            var userEmail by remember { mutableStateOf("") }
            var userRoleId by remember { mutableStateOf(1) }
            var userId by remember { mutableStateOf(1) } // Default 1
            
            // Sistema de Navegación Robusto (CARSIL-NAV)
            LaunchedEffect(authState) {
                when (val state = authState) {
                    is AuthState.Success -> {
                        if (!JwtTokenManager.isTokenValid(state.tokenJwt)) {
                            currentScreen = "LOGIN"
                            return@LaunchedEffect
                        }

                        // Extraer datos del JWT
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
                                intranetViewModel.loadAllowedMenus(userRoleId)

                                // Navegación instantánea según Rol
                                currentScreen = if (userRoleId == 2) "ATTENDANCE" else "DASHBOARD"
                            } catch (e: Exception) {
                                // Si hay error en el JWT, volver a login
                                currentScreen = "LOGIN"
                            }
                        }
                    }
                    is AuthState.Idle, is AuthState.Error, is AuthState.LockedOut -> {
                        if (currentScreen != "PROFORMA" && currentScreen != "CLIENTS" && currentScreen != "PRODUCTS") {
                            // Solo volvemos a LOGIN si no estamos en un sub-módulo persistente
                            currentScreen = "LOGIN"
                        }
                    }
                    else -> {}
                }
            }

            AppCarsilAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "DASHBOARD" -> DashboardScreen(
                            email = userEmail,
                            roleId = userRoleId,
                            allowedMenus = allowedMenus,
                            onGoToClients = { currentScreen = "CLIENTS" },
                            onGoToProducts = { currentScreen = "PRODUCTS" },
                            onGoToProforma = { currentScreen = "PROFORMA" },
                            onLogout = { 
                                intranetViewModel.clearSessionState()
                                currentScreen = "LOGIN"
                            }
                        )
                        "CLIENTS" -> ClientsScreen(
                            viewModel = intranetViewModel,
                            onBack = { currentScreen = "DASHBOARD" }
                        )
                        "PRODUCTS" -> ProductsScreen(
                            viewModel = intranetViewModel,
                            onBack = { currentScreen = "DASHBOARD" }
                        )
                        "PROFORMA" -> ProformaScreen(
                            viewModel = intranetViewModel,
                            idUsuario = userId,
                            onBack = { currentScreen = "DASHBOARD" }
                        )
                        "ATTENDANCE" -> AttendanceScreen(
                            email = userEmail,
                            viewModel = intranetViewModel,
                            idUsuario = userId,
                            onLogout = {
                                intranetViewModel.clearSessionState()
                                currentScreen = "LOGIN"
                            }
                        )
                        else -> LoginScreen(viewModel = authViewModel)
                    }
                }
            }
        }
    }
}