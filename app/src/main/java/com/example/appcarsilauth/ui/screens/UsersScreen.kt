package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: IntranetViewModel,
    onBack: () -> Unit,
    onNavigateToCreateForm: () -> Unit = {},
    onNavigateToEditForm: (user: Map<String, Any>) -> Unit = {}
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val roles by viewModel.roles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var estadoFiltro by rememberSaveable { mutableStateOf("TODOS") }

    LaunchedEffect(Unit) {
        viewModel.loadRolesUsuarios()
    }

    LaunchedEffect(searchQuery, estadoFiltro) {
        viewModel.loadAllUsuarios(searchQuery, estadoFiltro)
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Usuarios del Sistema",
                            fontWeight = FontWeight.Black,
                            color = CarsilColors.TextPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            "Gestion de cuentas y roles",
                            color = CarsilColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CarsilColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CarsilColors.Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = CarsilColors.Primary,
                    trackColor = CarsilColors.PrimaryLight
                )
            }

            uiMessage?.let { message ->
                Surface(
                    color = if (message.contains("Error") || message.contains("No se pudo")) {
                        CarsilColors.DangerLight
                    } else {
                        CarsilColors.PrimaryLight
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarsilColors.Stroke)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.weight(1f),
                            color = CarsilColors.TextPrimary,
                            fontSize = 13.sp
                        )
                        IconButton(onClick = { viewModel.clearUiMessage() }) {
                            Icon(Icons.Default.Close, null, tint = CarsilColors.TextMuted)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        "Buscar por nombre, documento o correo...",
                        color = CarsilColors.TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = CarsilColors.Primary) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CarsilColors.TextPrimary,
                    unfocusedTextColor = CarsilColors.TextPrimary,
                    focusedContainerColor = CarsilColors.Surface,
                    unfocusedContainerColor = CarsilColors.Surface,
                    focusedBorderColor = CarsilColors.Primary,
                    unfocusedBorderColor = CarsilColors.Stroke
                )
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                val compactTopActions = maxWidth < 360.dp

                if (compactTopActions) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Registros: ${usuarios.size}",
                            fontSize = 12.sp,
                            color = CarsilColors.TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedButton(
                            onClick = { onNavigateToCreateForm() },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CarsilColors.Primary),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, null, tint = CarsilColors.Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nuevo Usuario", color = CarsilColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Registros: ${usuarios.size}",
                            fontSize = 12.sp,
                            color = CarsilColors.TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedButton(
                            onClick = { onNavigateToCreateForm() },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CarsilColors.Primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, null, tint = CarsilColors.Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nuevo", color = CarsilColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = estadoFiltro == "TODOS",
                    onClick = { estadoFiltro = "TODOS" },
                    label = { Text("Todos", fontSize = 12.sp) },
                    border = BorderStroke(1.dp, if (estadoFiltro == "TODOS") CarsilColors.Primary else CarsilColors.Stroke),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CarsilColors.PrimaryLight,
                        selectedLabelColor = CarsilColors.Primary,
                        containerColor = CarsilColors.Surface,
                        labelColor = CarsilColors.TextSecondary
                    )
                )
                FilterChip(
                    selected = estadoFiltro == "ACTIVOS",
                    onClick = { estadoFiltro = "ACTIVOS" },
                    label = { Text("Activos", fontSize = 12.sp) },
                    border = BorderStroke(1.dp, if (estadoFiltro == "ACTIVOS") CarsilColors.Primary else CarsilColors.Stroke),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CarsilColors.PrimaryLight,
                        selectedLabelColor = CarsilColors.Primary,
                        containerColor = CarsilColors.Surface,
                        labelColor = CarsilColors.TextSecondary
                    )
                )
                FilterChip(
                    selected = estadoFiltro == "INACTIVOS",
                    onClick = { estadoFiltro = "INACTIVOS" },
                    label = { Text("Inactivos", fontSize = 12.sp) },
                    border = BorderStroke(1.dp, if (estadoFiltro == "INACTIVOS") CarsilColors.Primary else CarsilColors.Stroke),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CarsilColors.PrimaryLight,
                        selectedLabelColor = CarsilColors.Primary,
                        containerColor = CarsilColors.Surface,
                        labelColor = CarsilColors.TextSecondary
                    )
                )
            }

            if (usuarios.isEmpty() && isLoading.not()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ManageAccounts,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = CarsilColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No hay usuarios para mostrar",
                            color = CarsilColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = usuarios,
                        key = { user -> mapIntValue(user, "IdUsuario") }
                    ) { user ->
                        UserCard(
                            user = user,
                            onEdit = { onNavigateToEditForm(user) },
                            onToggleStatus = {
                                val isActive = mapIntValue(user, "Estado", 1) == 1
                                viewModel.actualizarEstadoUsuario(
                                    idUsuario = mapIntValue(user, "IdUsuario"),
                                    nuevoEstado = if (isActive) 0 else 1
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: Map<String, Any>,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val nombres = mapTextValue(user, "Nombres")
    val apellidos = mapTextValue(user, "Apellidos")
    val correo = mapTextValue(user, "Correo", "Sin correo")
    val documento = mapTextValue(user, "NumeroDocumento")
    val rol = mapTextValue(user, "RolNombre", "Sin rol")
    val isActive = mapIntValue(user, "Estado", 1) == 1

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CarsilColors.Surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CarsilColors.Stroke)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$nombres $apellidos",
                        fontWeight = FontWeight.ExtraBold,
                        color = CarsilColors.TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = rol,
                        color = CarsilColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = if (isActive) CarsilColors.SuccessLight else CarsilColors.DangerLight,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isActive) CarsilColors.Success.copy(alpha = 0.45f) else CarsilColors.Danger.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = if (isActive) "Activo" else "Inactivo",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = if (isActive) CarsilColors.Success else CarsilColors.Danger,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = CarsilColors.Stroke)

            Text(
                text = "Doc: $documento",
                fontSize = 12.sp,
                color = CarsilColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = correo,
                fontSize = 12.sp,
                color = CarsilColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CarsilColors.Stroke),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = CarsilColors.Primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar", color = CarsilColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isActive) CarsilColors.Danger else CarsilColors.Success),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        if (isActive) Icons.Default.Close else Icons.Default.LockOpen,
                        null,
                        tint = if (isActive) CarsilColors.Danger else CarsilColors.Success
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isActive) "Inactivar" else "Activar",
                        color = if (isActive) CarsilColors.Danger else CarsilColors.Success,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = CarsilColors.TextPrimary,
    unfocusedTextColor = CarsilColors.TextPrimary,
    focusedContainerColor = CarsilColors.Surface,
    unfocusedContainerColor = CarsilColors.Surface,
    focusedBorderColor = CarsilColors.Primary,
    unfocusedBorderColor = CarsilColors.Stroke
)

private fun mapIntValue(source: Map<String, Any>?, key: String, fallback: Int = 0): Int {
    val raw = source?.get(key)
    return when (raw) {
        is Int -> raw
        is Number -> raw.toInt()
        is String -> raw.toIntOrNull() ?: fallback
        else -> fallback
    }
}

private fun mapTextValue(source: Map<String, Any>?, key: String, fallback: String = ""): String {
    val raw = source?.get(key)
    return when (raw) {
        is String -> raw
        null -> fallback
        else -> raw.toString()
    }
}
