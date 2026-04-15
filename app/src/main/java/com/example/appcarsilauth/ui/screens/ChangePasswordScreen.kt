package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.components.CarsilShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    userId: Int,
    roleId: Int,
    onChangePassword: (String, String, (Boolean, String) -> Unit) -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var isCurrentPassVisible by remember { mutableStateOf(false) }
    var isNewPassVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isChanging by remember { mutableStateOf(false) }
    var showReLoginDialog by remember { mutableStateOf(false) }

    if (showReLoginDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Contraseña actualizada",
                    fontWeight = FontWeight.Bold,
                    color = CarsilColors.TextPrimary
                )
            },
            text = {
                Text(
                    "Por seguridad se cerrará la sesión. Inicia nuevamente con tu nueva contraseña.",
                    color = CarsilColors.TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReLoginDialog = false
                        onSuccess()
                    },
                    shape = CarsilShapes.Small,
                    colors = ButtonDefaults.buttonColors(containerColor = CarsilColors.Primary)
                ) {
                    Text("ENTENDIDO", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            containerColor = Color.White,
            tonalElevation = 0.dp,
            shape = CarsilShapes.Medium
        )
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Cambiar Contraseña", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = CarsilColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CarsilColors.Background,
                    titleContentColor = CarsilColors.TextPrimary
                )
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CarsilColors.Background)
        ) {
            val compactLayout = maxWidth < 380.dp
            val horizontalPadding = if (compactLayout) 16.dp else 24.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera descriptiva (Minimalista)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Actualizar Credenciales",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Por seguridad, tu nueva contraseña debe ser diferente a la anterior y tener al menos 6 caracteres.",
                        fontSize = 13.sp,
                        color = CarsilColors.TextSecondary,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Formulario Flat
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .background(CarsilColors.Surface, CarsilShapes.Medium)
                        .border(1.dp, CarsilColors.Stroke, CarsilShapes.Medium)
                        .padding(if (compactLayout) 16.dp else 20.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    FlatOutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = "Contraseña Actual",
                        placeholder = "Escribe tu clave actual",
                        isVisible = isCurrentPassVisible,
                        onToggleVisibility = { isCurrentPassVisible = !isCurrentPassVisible }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FlatOutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = "Nueva Contraseña",
                        placeholder = "Mínimo 6 caracteres",
                        isVisible = isNewPassVisible,
                        onToggleVisibility = { isNewPassVisible = !isNewPassVisible }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (newPass.length < 6) {
                                errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                return@Button
                            }
                            isChanging = true
                            errorMessage = null
                            onChangePassword(currentPass, newPass) { success, msg ->
                                isChanging = false
                                if (success) {
                                    if (roleId == 1) {
                                        showReLoginDialog = true
                                    } else {
                                        onSuccess()
                                    }
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CarsilShapes.Small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CarsilColors.Primary,
                            contentColor = Color.Black,
                            disabledContainerColor = CarsilColors.Gray400
                        ),
                        enabled = !isChanging && currentPass.isNotBlank() && newPass.isNotBlank()
                    ) {
                        if (isChanging) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("ACTUALIZAR CONTRASEÑA", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlatOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isVisible: Boolean = false,
    onToggleVisibility: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CarsilColors.TextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            visualTransformation = if (isVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isVisible) "Ocultar" else "Mostrar",
                        tint = CarsilColors.Primary
                    )
                }
            },
            singleLine = true,
            shape = CarsilShapes.Small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CarsilColors.Primary,
                unfocusedBorderColor = CarsilColors.Stroke,
                focusedTextColor = CarsilColors.TextPrimary,
                unfocusedTextColor = CarsilColors.TextPrimary,
                cursorColor = CarsilColors.Primary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
