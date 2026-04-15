package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    viewModel: IntranetViewModel,
    isEditMode: Boolean = false,
    initialUser: Map<String, Any>? = null,
    onBack: () -> Unit
) {
    val roles by viewModel.roles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val roleOptions = remember(roles) {
        roles.mapNotNull { role ->
            val id = mapIntValue(role, "IdRol", -1)
            val label = mapTextValue(role, "Descripcion")
            if (id > 0) RoleOption(id = id, label = label) else null
        }
    }

    var nombres by rememberSaveable(initialUser) { mutableStateOf(mapTextValue(initialUser, "Nombres")) }
    var apellidos by rememberSaveable(initialUser) { mutableStateOf(mapTextValue(initialUser, "Apellidos")) }
    var tipoDocumento by rememberSaveable(initialUser) { mutableStateOf(mapTextValue(initialUser, "TipoDocumento", "DNI")) }
    var numeroDocumento by rememberSaveable(initialUser) { mutableStateOf(mapTextValue(initialUser, "NumeroDocumento")) }
    var correo by rememberSaveable(initialUser) { mutableStateOf(mapTextValue(initialUser, "Correo")) }
    var telefono by rememberSaveable(initialUser) { mutableStateOf(mapTextValue(initialUser, "Telefono")) }
    var direccion by rememberSaveable(initialUser) { mutableStateOf(mapTextValue(initialUser, "Direccion")) }
    var selectedRole by rememberSaveable(initialUser, roleOptions) {
        mutableIntStateOf(
            mapIntValue(initialUser, "IdRol", roleOptions.firstOrNull()?.id ?: 1)
        )
    }
    var estado by rememberSaveable(initialUser) { mutableIntStateOf(mapIntValue(initialUser, "Estado", 1)) }

    var clave by rememberSaveable { mutableStateOf("") }
    var confirmarClave by rememberSaveable { mutableStateOf("") }

    var roleExpanded by remember { mutableStateOf(false) }
    var showClave by rememberSaveable(initialUser) { mutableStateOf(false) }
    var showConfirmarClave by rememberSaveable(initialUser) { mutableStateOf(false) }

    // Validación de DNI
    var dniError by remember { mutableStateOf("") }
    // Validación de teléfono
    var telefonoError by remember { mutableStateOf("") }

    fun validarDNI(dni: String): Boolean {
        return dni.trim().length == 8 && dni.trim().all { it.isDigit() }
    }

    fun validarTelefono(tel: String): Boolean {
        val cleaned = tel.trim().replace(Regex("[^0-9]"), "")
        return cleaned.length == 9 && cleaned.all { it.isDigit() }
    }

    fun handleSubmit() {
        dniError = ""
        telefonoError = ""

        if (!validarDNI(numeroDocumento)) {
            dniError = "El DNI debe tener exactamente 8 dígitos"
            return
        }

        if (telefono.isNotEmpty() && !validarTelefono(telefono)) {
            telefonoError = "El teléfono debe tener exactamente 9 dígitos"
            return
        }

        if (isEditMode && initialUser != null) {
            val userId = mapIntValue(initialUser, "IdUsuario")
            viewModel.actualizarUsuario(
                idUsuario = userId,
                nombres = nombres,
                apellidos = apellidos,
                tipoDocumento = tipoDocumento,
                numeroDocumento = numeroDocumento,
                correo = correo,
                telefono = telefono,
                direccion = direccion,
                idRol = selectedRole,
                estado = estado,
                nuevaClave = clave,
                confirmarClave = confirmarClave
            )
        } else {
            viewModel.registrarUsuario(
                nombres = nombres,
                apellidos = apellidos,
                tipoDocumento = tipoDocumento,
                numeroDocumento = numeroDocumento,
                correo = correo,
                telefono = telefono,
                direccion = direccion,
                idRol = selectedRole,
                estado = estado,
                clave = clave,
                confirmarClave = confirmarClave
            )
        }
        onBack()
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isEditMode) "Editar Usuario" else "Crear Nuevo Usuario",
                            fontWeight = FontWeight.Black,
                            color = CarsilColors.TextPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            if (isEditMode) "Actualiza información del usuario" else "Registra un nuevo miembro",
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
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = CarsilColors.TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección Personal
                SectionTitle("Información Personal")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nombres,
                        onValueChange = { nombres = it },
                        label = { Text("Nombres") },
                        modifier = Modifier.weight(1f),
                        colors = formFieldColors()
                    )
                    OutlinedTextField(
                        value = apellidos,
                        onValueChange = { apellidos = it },
                        label = { Text("Apellidos") },
                        modifier = Modifier.weight(1f),
                        colors = formFieldColors()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tipoDocumento,
                        onValueChange = { tipoDocumento = it },
                        label = { Text("Tipo Doc.") },
                        modifier = Modifier.weight(1f),
                        colors = formFieldColors()
                    )
                    OutlinedTextField(
                        value = numeroDocumento,
                        onValueChange = { numeroDocumento = it.take(8) },
                        label = { Text("Número (8 dígitos)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = dniError.isNotEmpty(),
                        colors = formFieldColors()
                    )
                }
                if (dniError.isNotEmpty()) {
                    Text(dniError, color = CarsilColors.Danger, fontSize = 11.sp)
                }

                HorizontalDivider(color = CarsilColors.Stroke)

                // Sección Contacto
                SectionTitle("Información de Contacto")
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = formFieldColors()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it.take(9).filter { c -> c.isDigit() || c == '+' || c == ' ' || c == '-' }
                    },
                    label = { Text("Teléfono (9 dígitos)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = telefonoError.isNotEmpty(),
                    colors = formFieldColors()
                )
                if (telefonoError.isNotEmpty()) {
                    Text(telefonoError, color = CarsilColors.Danger, fontSize = 11.sp)
                }

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = formFieldColors()
                )

                HorizontalDivider(color = CarsilColors.Stroke)

                // Sección Rol y Estado
                SectionTitle("Rol del Sistema")
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
                }

                SectionTitle("Estado del Usuario")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
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
                        leadingIcon = { if (estado == 1) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
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
                        leadingIcon = { if (estado == 0) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    )
                }

                HorizontalDivider(color = CarsilColors.Stroke)

                // Sección Credenciales
                SectionTitle(if (isEditMode) "Actualizar Contraseña (Opcional)" else "Credenciales")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.weight(1f),
                        visualTransformation = if (showClave) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showClave = !showClave }) {
                                Icon(
                                    if (showClave) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null,
                                    tint = CarsilColors.TextMuted
                                )
                            }
                        },
                        colors = formFieldColors()
                    )
                    OutlinedTextField(
                        value = confirmarClave,
                        onValueChange = { confirmarClave = it },
                        label = { Text("Confirmar") },
                        modifier = Modifier.weight(1f),
                        visualTransformation = if (showConfirmarClave) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmarClave = !showConfirmarClave }) {
                                Icon(
                                    if (showConfirmarClave) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null,
                                    tint = CarsilColors.TextMuted
                                )
                            }
                        },
                        colors = formFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CarsilColors.Stroke)
                    ) {
                        Text("Cancelar", color = CarsilColors.TextPrimary)
                    }
                    Button(
                        onClick = ::handleSubmit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CarsilColors.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(if (isEditMode) "Guardar cambios" else "Registrar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = CarsilColors.TextMuted
    )
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = CarsilColors.TextPrimary,
    unfocusedTextColor = CarsilColors.TextPrimary,
    focusedContainerColor = CarsilColors.Surface,
    unfocusedContainerColor = CarsilColors.Surface,
    focusedBorderColor = CarsilColors.Primary,
    unfocusedBorderColor = CarsilColors.Stroke,
    errorBorderColor = CarsilColors.Danger,
    errorContainerColor = CarsilColors.DangerLight
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
