package com.example.appcarsilauth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appcarsilauth.data.local.AppDatabase
import com.example.appcarsilauth.data.repository.SecurityRepositoryImpl
import com.example.appcarsilauth.domain.use_case.ValidateLockoutUseCase
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.components.CarsilShapes
import com.example.appcarsilauth.ui.screens.*
import com.example.appcarsilauth.ui.theme.AppCarsilAuthTheme
import com.example.appcarsilauth.ui.viewmodel.AuthState
import com.example.appcarsilauth.ui.viewmodel.AuthViewModel
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel
import com.example.appcarsilauth.util.BiometricHelper
import com.example.appcarsilauth.util.JwtTokenManager
import com.example.appcarsilauth.util.LoginPreferences
import com.example.appcarsilauth.util.SecureBiometricStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val lastActivityTimeState = mutableLongStateOf(System.currentTimeMillis())

    override fun onUserInteraction() {
        super.onUserInteraction()
        lastActivityTimeState.longValue = System.currentTimeMillis()
    }

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
        val loginPreferences = LoginPreferences(this)
        val biometricHelper = BiometricHelper(this)

        val authViewModel: AuthViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(
                        validateLockoutUseCase, 
                        database.auditDao(), 
                        database.intranetDao(),
                        loginPreferences
                    ) as T
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
            var userBeingEdited by remember { mutableStateOf<Map<String, Any>?>(null) }
            val homeScreen = if (userRoleId == 2) "ATTENDANCE" else "DASHBOARD"

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

            val inactivityWarningThresholdMs = 4 * 60 * 1000L
            val inactivitySessionTimeoutMs = 5 * 60 * 1000L
            var isSessionExpired by remember { mutableStateOf(false) }
            var showInactivityWarning by remember { mutableStateOf(false) }
            var inactivitySecondsLeft by remember { mutableIntStateOf(0) }
            var showExitDialog by remember { mutableStateOf(false) }
            val isLoading by intranetViewModel.isLoading.collectAsState()

            // CARSIL-POL-SEC: Control de inactividad (4m Aviso, 5m Cierre)
            LaunchedEffect(authState) {
                if (authState is AuthState.Success) {
                    while (true) {
                        delay(1000)
                        val elapsed = System.currentTimeMillis() - lastActivityTimeState.longValue
                        val remainingMs = (inactivitySessionTimeoutMs - elapsed).coerceAtLeast(0L)
                        inactivitySecondsLeft = kotlin.math.ceil(remainingMs / 1000.0).toInt()

                        when {
                            elapsed >= inactivitySessionTimeoutMs && !isSessionExpired -> {
                                showInactivityWarning = false
                                inactivitySecondsLeft = 0
                                isSessionExpired = true
                                authViewModel.logout()
                            }
                            elapsed >= inactivityWarningThresholdMs && !isSessionExpired -> {
                                showInactivityWarning = true
                            }
                            else -> {
                                showInactivityWarning = false
                            }
                        }
                    }
                } else {
                    showInactivityWarning = false
                    inactivitySecondsLeft = 0
                }
            }

            val dashboardStats by intranetViewModel.dashboardStats.collectAsState()
            val recentProformas by intranetViewModel.proformas.collectAsState()

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

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
                                userName = state.userName

                                intranetViewModel.loadAllowedMenus(userRoleId)
                                intranetViewModel.loadDashboardData()
                                
                                currentScreen = homeScreen
                                lastActivityTimeState.longValue = System.currentTimeMillis()
                            } catch (e: Exception) {
                                currentScreen = "LOGIN"
                            }
                        }
                    }
                    is AuthState.Idle, is AuthState.Error, is AuthState.LockedOut -> {
                        if (currentScreen != "PROFORMA" && currentScreen != "CLIENTS" && currentScreen != "PRODUCTS" && currentScreen != "PROFILE" && currentScreen != "CHANGE_PASSWORD" && currentScreen != "REPORTS" && currentScreen != "FACTURAS" && currentScreen != "USERS") {
                            currentScreen = "LOGIN"
                        }
                    }
                    else -> {}
                }
            }

            // CARSIL-POL-SEC: El empleado nunca debe navegar al panel principal u otros módulos administrativos.
            LaunchedEffect(authState, userRoleId, currentScreen) {
                if (authState is AuthState.Success) {
                    if (
                        userRoleId == 2 &&
                        currentScreen in setOf("DASHBOARD", "CLIENTS", "PRODUCTS", "PROFORMA", "FACTURAS", "REPORTS", "USERS")
                    ) {
                        currentScreen = "ATTENDANCE"
                    }

                    if (currentScreen == "USERS" && userRoleId != 1) {
                        currentScreen = homeScreen
                    }
                }
            }

            // Refresco automatico para que los modulos del dashboard se actualicen casi en tiempo real.
            LaunchedEffect(authState, currentScreen) {
                if (authState is AuthState.Success && currentScreen == "DASHBOARD") {
                    while (true) {
                        intranetViewModel.refreshDashboardRealtime()
                        delay(8000)
                    }
                }
            }

            AppCarsilAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    // OVERLAY MINIMALISTA DE SESIÓN EXPIRADA
                    if (isSessionExpired) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.95f))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.TimerOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = CarsilColors.TextSecondary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    "Sesión Finalizada",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CarsilColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Tu sesión se cerró automáticamente después de 5 minutos de inactividad por tu seguridad.",
                                    fontSize = 14.sp,
                                    color = CarsilColors.TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(40.dp))
                                Button(
                                    onClick = {
                                        isSessionExpired = false
                                        currentScreen = "LOGIN"
                                    },
                                    shape = CarsilShapes.Medium,
                                    colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Primary),
                                    modifier = Modifier.width(200.dp)
                                ) {
                                    Text("ENTENDIDO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }

                    // ADVERTENCIA DE INACTIVIDAD (CARSIL-POL-INACTIVITY)
                    if (showInactivityWarning) {
                        val inactivityCountdownText = String.format(
                            "%02d:%02d",
                            inactivitySecondsLeft / 60,
                            inactivitySecondsLeft % 60
                        )

                        AlertDialog(
                            onDismissRequest = { /* No cerrar al tocar fuera por seguridad */ },
                            icon = { Icon(Icons.Default.Timer, contentDescription = null, tint = CarsilColors.Primary) },
                            title = { Text("Aviso de Inactividad", fontWeight = FontWeight.Bold) },
                            text = { 
                                Text(
                                    "Tu sesión se cerrará automáticamente en $inactivityCountdownText por inactividad. ¿Deseas permanecer conectado?",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ) 
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showInactivityWarning = false
                                        lastActivityTimeState.longValue = System.currentTimeMillis()
                                        inactivitySecondsLeft = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Primary),
                                    shape = CarsilShapes.Medium
                                ) {
                                    Text("PERMANECER")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showInactivityWarning = false
                                        inactivitySecondsLeft = 0
                                        authViewModel.logout()
                                        isSessionExpired = true
                                    }
                                ) {
                                    Text("CERRAR SESIÓN", color = Color.Red)
                                }
                            },
                            containerColor = Color.White,
                            textContentColor = CarsilColors.TextPrimary,
                            titleContentColor = CarsilColors.TextPrimary
                        )
                    }
                    
                    // DIALOGO DE ADVERTENCIA SEGURIDAD (BLOQUEO)
                    if (showTransferDialog) {
                        AlertDialog(
                            onDismissRequest = { showTransferDialog = false },
                            title = { 
                                Text(
                                    "Seguridad de Cuenta", 
                                    fontWeight = FontWeight.Bold, 
                                    color = CarsilColors.TextPrimary 
                                ) 
                            },
                            text = { 
                                Text(
                                    "Otra cuenta ya tiene activa la huella dactilar en este dispositivo. Por seguridad, debe desactivarla desde la otra cuenta primero.", 
                                    color = CarsilColors.TextSecondary,
                                    fontSize = 14.sp
                                ) 
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showTransferDialog = false },
                                    shape = CarsilShapes.Small,
                                    colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Primary)
                                ) { Text("Entendido", fontWeight = FontWeight.Bold) }
                            },
                            containerColor = CarsilColors.Surface,
                            tonalElevation = 0.dp
                        )
                    }

                    // DIALOGO DE CONFIRMACION PARA SALIR DEL SISTEMA
                    if (showExitDialog) {
                        AlertDialog(
                            onDismissRequest = { showExitDialog = false },
                            title = { 
                                Text(
                                    "¿Salir del Sistema?", 
                                    fontWeight = FontWeight.Bold, 
                                    color = CarsilColors.TextPrimary 
                                ) 
                            },
                            text = { 
                                Text(
                                    "¿Estás seguro que deseas cerrar la aplicación? Se perderán las proformas no guardadas.", 
                                    color = CarsilColors.TextSecondary,
                                    fontSize = 14.sp
                                ) 
                            },
                            confirmButton = {
                                Button(
                                    onClick = { finish() },
                                    shape = CarsilShapes.Small,
                                    colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Danger)
                                ) { Text("SÍ, SALIR", fontWeight = FontWeight.Bold) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExitDialog = false }) { 
                                    Text("CANCELAR", color = CarsilColors.TextSecondary, fontWeight = FontWeight.Medium) 
                                }
                            },
                            containerColor = CarsilColors.Surface,
                            tonalElevation = 0.dp
                        )
                    }

                    // MANEJO BOTON DE ATRAS Dinámico
                    BackHandler {
                        if (drawerState.isOpen) {
                            scope.launch { drawerState.close() }
                        } else if (currentScreen == "DASHBOARD" || currentScreen == "ATTENDANCE" || currentScreen == "LOGIN" || currentScreen == "REPORTS") {
                            showExitDialog = true
                        } else {
                            // En pantallas secundarias volver a la pantalla home según el rol
                            currentScreen = homeScreen
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentScreen != "LOGIN",
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = Color.White,
                                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                                modifier = Modifier.width(210.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp)) {
                                    // Sidebar minimalista sin información redundante de usuario
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text("GESTIÓN PRINCIPAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Secciones con Restricciones de Rol (CARSIL-POL-SEC)
                                    if (userRoleId != 2) {
                                        // ADMINISTRADOR / OTROS
                                        NavigationDrawerItem(
                                            label = { Text("Panel Principal", fontWeight = FontWeight.Medium) },
                                            selected = currentScreen == "DASHBOARD",
                                            onClick = { 
                                                currentScreen = "DASHBOARD"
                                                scope.launch { drawerState.close() }
                                            },
                                            icon = { Icon(Icons.Default.Dashboard, null) },
                                            colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CarsilColors.PrimaryLight, selectedTextColor = CarsilColors.Primary, selectedIconColor = CarsilColors.Primary)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        NavigationDrawerItem(
                                            label = { Text("Asistencia Diaria", fontWeight = FontWeight.Medium) },
                                            selected = currentScreen == "ATTENDANCE",
                                            onClick = { 
                                                currentScreen = "ATTENDANCE"
                                                scope.launch { drawerState.close() }
                                            },
                                            icon = { Icon(Icons.Default.Timer, null) }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        NavigationDrawerItem(
                                            label = { Text("Gestión Clientes", fontWeight = FontWeight.Medium) },
                                            selected = currentScreen == "CLIENTS",
                                            onClick = { 
                                                currentScreen = "CLIENTS"
                                                scope.launch { drawerState.close() }
                                            },
                                            icon = { Icon(Icons.Default.Groups, null) }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        NavigationDrawerItem(
                                            label = { Text("Inventario Stock", fontWeight = FontWeight.Medium) },
                                            selected = currentScreen == "PRODUCTS",
                                            onClick = { 
                                                currentScreen = "PRODUCTS"
                                                scope.launch { drawerState.close() }
                                            },
                                            icon = { Icon(Icons.Default.Inventory, null) }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        NavigationDrawerItem(
                                            label = { Text("Generar Proforma", fontWeight = FontWeight.Medium) },
                                            selected = currentScreen == "PROFORMA",
                                            onClick = { 
                                                currentScreen = "PROFORMA"
                                                scope.launch { drawerState.close() }
                                            },
                                            icon = { Icon(Icons.Default.ReceiptLong, null) }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        NavigationDrawerItem(
                                            label = { Text("Facturas", fontWeight = FontWeight.Medium) },
                                            selected = currentScreen == "FACTURAS",
                                            onClick = { 
                                                currentScreen = "FACTURAS"
                                                scope.launch { drawerState.close() }
                                            },
                                            icon = { Icon(Icons.Default.Receipt, null) },
                                            colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = Color(0xFFEDE9FE), selectedTextColor = Color(0xFF6366F1), selectedIconColor = Color(0xFF6366F1))
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (userRoleId == 1) {
                                            NavigationDrawerItem(
                                                label = { Text("Usuarios Sistema", fontWeight = FontWeight.Medium) },
                                                selected = currentScreen == "USERS",
                                                onClick = {
                                                    currentScreen = "USERS"
                                                    scope.launch { drawerState.close() }
                                                },
                                                icon = { Icon(Icons.Default.ManageAccounts, null) }
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        NavigationDrawerItem(
                                            label = { Text("Reportes Analíticos", fontWeight = FontWeight.Medium) },
                                            selected = currentScreen == "REPORTS",
                                            onClick = { 
                                                currentScreen = "REPORTS"
                                                scope.launch { drawerState.close() }
                                            },
                                            icon = { Icon(Icons.Default.Assessment, null) }
                                        )
                                    } else {
                                        // EMPLEADO (Solo vista limitada)
                                        NavigationDrawerItem(
                                            label = { Text("Marcar Asistencia", fontWeight = FontWeight.Medium) },
                                            selected = currentScreen == "ATTENDANCE",
                                            onClick = { 
                                                currentScreen = "ATTENDANCE"
                                                scope.launch { drawerState.close() }
                                            },
                                            icon = { Icon(Icons.Default.Timer, null) },
                                            colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CarsilColors.PrimaryLight, selectedTextColor = CarsilColors.Primary, selectedIconColor = CarsilColors.Primary)
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    NavigationDrawerItem(
                                        label = { Text("Mi Perfil", fontWeight = FontWeight.Medium) },
                                        selected = currentScreen == "PROFILE",
                                        onClick = { 
                                            currentScreen = "PROFILE"
                                            scope.launch { drawerState.close() }
                                        },
                                        icon = { Icon(Icons.Default.Person, null) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NavigationDrawerItem(
                                        label = { Text("Cerrar Sesión", fontWeight = FontWeight.Medium) },
                                        selected = false,
                                        onClick = { 
                                            authViewModel.logout()
                                            intranetViewModel.clearSessionState()
                                            currentScreen = "LOGIN"
                                            scope.launch { drawerState.close() }
                                        },
                                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = CarsilColors.Danger) }
                                    )
                                }
                            }
                        }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White
                        ) {
                            when (currentScreen) {
                                "DASHBOARD" -> DashboardScreen(
                                    email = userEmail,
                                    userName = userName,
                                    roleId = userRoleId,
                                    allowedMenus = allowedMenus,
                                    stats = dashboardStats,
                                    recentProformas = recentProformas,
                                    isLoading = isLoading,
                                    onGoToClients = { currentScreen = "CLIENTS" },
                                    onGoToProducts = { currentScreen = "PRODUCTS" },
                                    onGoToProforma = { currentScreen = "PROFORMA" },
                                    onGoToProfile = { currentScreen = "PROFILE" },
                                    onMenuClick = {
                                        scope.launch { drawerState.open() }
                                    },
                                    onLogout = {
                                        authViewModel.logout()
                                        intranetViewModel.clearSessionState()
                                        currentScreen = "LOGIN"
                                    },
                                    onRefresh = { intranetViewModel.refreshDashboardRealtime() }
                                )
                                "PROFILE" -> ProfileScreen(
                                    email = userEmail,
                                    userName = userName,
                                    roleId = userRoleId,
                                    isBiometricEnrolled = isBiometricEnrolled,
                                    isBiometricAvailable = canUseBiometric,
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
                                                    Toast.makeText(this@MainActivity, "Huella añadida", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { errorCode, _ ->
                                                    // No mostrar nada si el usuario cancela (10: ERROR_USER_CANCELED, 13: ERROR_NEGATIVE_BUTTON)
                                                    if (errorCode != 10 && errorCode != 13 && errorCode != 7) { // 7 es Lockout temporal
                                                        Toast.makeText(this@MainActivity, "Ocurrió un inconveniente con la biometría", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    onRemoveBiometric = {
                                        biometricStorage.removeEnrollment()
                                        isBiometricEnrolled = false
                                        Toast.makeText(this@MainActivity, "Huella desactivada", Toast.LENGTH_SHORT).show()
                                    },
                                    onBack = { currentScreen = homeScreen }
                                )
                                "CHANGE_PASSWORD" -> ChangePasswordScreen(
                                    userId = userId,
                                    roleId = userRoleId,
                                    onChangePassword = { current, new, onRes ->
                                        authViewModel.changePassword(userId, current, new, onRes)
                                    },
                                    onBack = { currentScreen = "PROFILE" },
                                    onSuccess = {
                                        authViewModel.logout()
                                        intranetViewModel.clearSessionState()
                                        currentScreen = "LOGIN"
                                    }
                                )
                                "CLIENTS" -> ClientsScreen(viewModel = intranetViewModel, onBack = { currentScreen = "DASHBOARD" })
                                "PRODUCTS" -> ProductsScreen(viewModel = intranetViewModel, onBack = { currentScreen = "DASHBOARD" })
                                "USERS" -> UsersScreen(
                                    viewModel = intranetViewModel,
                                    onBack = { currentScreen = "DASHBOARD" },
                                    onNavigateToCreateForm = { currentScreen = "USER_FORM_CREATE" },
                                    onNavigateToEditForm = { user ->
                                        userBeingEdited = user
                                        currentScreen = "USER_FORM_EDIT"
                                    }
                                )
                                "USER_FORM_CREATE" -> UserFormScreen(
                                    viewModel = intranetViewModel,
                                    isEditMode = false,
                                    initialUser = null,
                                    onBack = { currentScreen = "USERS" }
                                )
                                "USER_FORM_EDIT" -> UserFormScreen(
                                    viewModel = intranetViewModel,
                                    isEditMode = true,
                                    initialUser = userBeingEdited,
                                    onBack = {
                                        userBeingEdited = null
                                        currentScreen = "USERS"
                                    }
                                )
                                "PROFORMA" -> ProformaScreen(viewModel = intranetViewModel, idUsuario = userId, onBack = { currentScreen = "DASHBOARD" })
                                "FACTURAS" -> FacturasScreen(viewModel = intranetViewModel, idUsuario = userId, onBack = { currentScreen = "DASHBOARD" })
                                "REPORTS" -> ReportsScreen(viewModel = intranetViewModel, onBack = { currentScreen = "DASHBOARD" })
                                "ATTENDANCE" -> AttendanceScreen(
                                    userName = userName,
                                    roleId = userRoleId,
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
                                                    onError = { errorCode, _ ->
                                                        // No mostrar nada si el usuario cancela
                                                        if (errorCode != 10 && errorCode != 13 && errorCode != 7) {
                                                            Toast.makeText(this@MainActivity, "No se pudo verificar la huella", Toast.LENGTH_SHORT).show()
                                                        }
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
    }
}