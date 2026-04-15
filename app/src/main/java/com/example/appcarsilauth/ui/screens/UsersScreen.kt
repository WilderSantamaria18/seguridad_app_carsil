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

private data class RoleOption(val id: Int, val label: String)

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
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = CarsilColors.TextPrimary
                )

                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    label = { Text("Nombres") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = formFieldColors()
                )
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = formFieldColors()
                )

                if (compactDialog) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = tipoDocumento,
                            onValueChange = { tipoDocumento = it },
                            label = { Text("Tipo Doc.") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = formFieldColors()
                        )
                        OutlinedTextField(
                            value = numeroDocumento,
                            onValueChange = { numeroDocumento = it },
                            label = { Text("Número") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = formFieldColors()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tipoDocumento,
                            onValueChange = { tipoDocumento = it },
                            label = { Text("Tipo Doc.") },
                            modifier = Modifier.weight(1f),
                            colors = formFieldColors()
                        )
                        OutlinedTextField(
                            value = numeroDocumento,
                            onValueChange = { numeroDocumento = it },
                            label = { Text("Número") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = formFieldColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = formFieldColors()
                )

                if (compactDialog) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = formFieldColors()
                        )
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = formFieldColors()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = formFieldColors()
                        )
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección") },
                            modifier = Modifier.weight(1f),
                            colors = formFieldColors()
                        )
                    }
                }

                if (roleOptions.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = roleExpanded,
                        onExpandedChange = { roleExpanded = !roleExpanded }
                    ) {
                        val selectedRoleName = roleOptions.firstOrNull { it.id == selectedRole }?.label ?: "Seleccionar rol"
                        OutlinedTextField(
                            value = selectedRoleName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rol") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = formFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }
                        ) {
                            roleOptions.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.label) },
                                    onClick = {
                                        selectedRole = role.id
                                        roleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CarsilColors.DangerLight,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = CarsilColors.Danger,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("No se pudieron cargar los roles.", color = CarsilColors.TextPrimary)
                        }
                    }
                }

                Text(
                    text = "Estado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CarsilColors.TextMuted
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = estado == 1,
                        onClick = { estado = 1 },
                        label = { Text("Activo") },
                        border = BorderStroke(1.dp, if (estado == 1) CarsilColors.Primary else CarsilColors.Stroke),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CarsilColors.PrimaryLight,
                            selectedLabelColor = CarsilColors.Primary,
                            containerColor = CarsilColors.Surface,
                            labelColor = CarsilColors.TextSecondary
                        ),
                        leadingIcon = {
                            if (estado == 1) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    FilterChip(
                        selected = estado == 0,
                        onClick = { estado = 0 },
                        label = { Text("Inactivo") },
                        border = BorderStroke(1.dp, if (estado == 0) CarsilColors.Primary else CarsilColors.Stroke),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CarsilColors.PrimaryLight,
                            selectedLabelColor = CarsilColors.Primary,
                            containerColor = CarsilColors.Surface,
                            labelColor = CarsilColors.TextSecondary
                        ),
                        leadingIcon = {
                            if (estado == 0) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }

                HorizontalDivider(color = CarsilColors.Stroke)

                Text(
                    text = if (initialUser == null) "Credenciales" else "Actualizar contraseña (opcional)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CarsilColors.TextMuted
                )

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showClave) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showClave = !showClave }) {
                            Icon(
                                imageVector = if (showClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showClave) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = CarsilColors.TextSecondary
                            )
                        }
                    },
                    colors = formFieldColors()
                )
                OutlinedTextField(
                    value = confirmarClave,
                    onValueChange = { confirmarClave = it },
                    label = { Text("Confirmar contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showConfirmarClave) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmarClave = !showConfirmarClave }) {
                            Icon(
                                imageVector = if (showConfirmarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmarClave) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = CarsilColors.TextSecondary
                            )
                        }
                    },
                    colors = formFieldColors()
                )

                if (compactDialog) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (roleOptions.isNotEmpty()) {
                                    onSubmit(
                                        nombres,
                                        apellidos,
                                        tipoDocumento,
                                        numeroDocumento,
                                        correo,
                                        telefono,
                                        direccion,
                                        selectedRole,
                                        estado,
                                        clave,
                                        confirmarClave
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CarsilColors.Primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 11.dp)
                        ) {
                            Text(submitLabel)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CarsilColors.Stroke),
                            contentPadding = PaddingValues(vertical = 11.dp)
                        ) {
                            Text("Cancelar", color = CarsilColors.TextPrimary)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CarsilColors.Stroke),
                            contentPadding = PaddingValues(vertical = 11.dp)
                        ) {
                            Text("Cancelar", color = CarsilColors.TextPrimary)
                        }
                        Button(
                            onClick = {
                                if (roleOptions.isNotEmpty()) {
                                    onSubmit(
                                        nombres,
                                        apellidos,
                                        tipoDocumento,
                                        numeroDocumento,
                                        correo,
                                        telefono,
                                        direccion,
                                        selectedRole,
                                        estado,
                                        clave,
                                        confirmarClave
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CarsilColors.Primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 11.dp)
                        ) {
                            Text(submitLabel)
                        }
                    }
                }
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
