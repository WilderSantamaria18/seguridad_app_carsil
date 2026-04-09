package com.example.appcarsilauth.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.CaptchaComponent
import com.example.appcarsilauth.ui.viewmodel.AuthState
import com.example.appcarsilauth.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    isBiometricEnrolledForThisUser: Boolean,
    onFingerprintClick: () -> Unit = {}
) {
    val authState by viewModel.authState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Black.copy(alpha = 0.03f),
                    radius = 400f,
                    center = Offset(size.width * 0.9f, size.height * 0.1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(
                    targetState = viewModel.loginStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "stepAnimation"
                ) { step ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (step == 1) {
                            // EL LOGO SOLO APARECE EN EL PASO 1
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.appcarsilauth.R.drawable.logo_carsil),
                                contentDescription = "Logo Carsil",
                                modifier = Modifier
                                    .size(320.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(0.dp))

                            Text(
                                text = "Gestión Empresarial Inteligente\nIngresa tu correo para comenzar.",
                                fontSize = 15.sp,
                                color = Color(0xFF455A64),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = viewModel.email,
                                onValueChange = { viewModel.onEmailChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Usuario o Correo", color = Color(0xFF607D8B)) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = Color.Black) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = Color.Black,
                                    focusedLabelColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { viewModel.identifyUser() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .shadow(8.dp, RoundedCornerShape(29.dp)),
                                shape = RoundedCornerShape(29.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                if (authState is AuthState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Identificarme", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // PASO 2: BIENVENIDA (SIN LOGO)
                            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.backToStep1() },
                                        modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text("Cambiar cuenta", fontSize = 14.sp, color = Color(0xFF455A64))
                                }
                                
                                Spacer(modifier = Modifier.height(40.dp))
                                
                                Text(
                                    text = "Bienvenido al sistema,",
                                    fontSize = 18.sp,
                                    color = Color(0xFF455A64)
                                )
                                Text(
                                    text = viewModel.identifiedUserName,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    lineHeight = 36.sp
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                OutlinedTextField(
                                    value = viewModel.pin,
                                    onValueChange = { viewModel.onPinChange(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Ingresa tu contraseña", color = Color(0xFF607D8B)) },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.Black)
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedBorderColor = Color.Black,
                                        unfocusedBorderColor = Color.Gray,
                                        cursorColor = Color.Black,
                                        focusedLabelColor = Color.Black
                                    )
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { viewModel.preVerifyLogin() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp)
                                        .shadow(8.dp, RoundedCornerShape(29.dp)),
                                    shape = RoundedCornerShape(29.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Black,
                                        contentColor = Color.White
                                    )
                                ) {
                                    if (authState is AuthState.Loading && !viewModel.isCaptchaModalVisible) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(30.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isBiometricEnrolledForThisUser) Color.White else Color.Transparent)
                                        .border(
                                            1.dp, 
                                            if (isBiometricEnrolledForThisUser) Color.Black.copy(alpha = 0.1f) else Color.Transparent, 
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable(enabled = isBiometricEnrolledForThisUser) { onFingerprintClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Fingerprint, 
                                            null, 
                                            modifier = Modifier.size(32.dp),
                                            tint = if (isBiometricEnrolledForThisUser) Color.Black else Color.Gray.copy(alpha = 0.4f)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = if (isBiometricEnrolledForThisUser) "Acceder con Biometría" else "Huella no configurada",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBiometricEnrolledForThisUser) Color.Black else Color.Gray.copy(alpha = 0.4f),
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color.Red,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (viewModel.isCaptchaModalVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCaptchaModal() },
            title = { Text("Seguridad Adicional", fontWeight = FontWeight.ExtraBold, color = Color.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Solo un paso más. Resuelve el captcha para verificar tu ingreso.", color = Color(0xFF263238))
                    Spacer(Modifier.height(20.dp))
                    CaptchaComponent(onCaptchaGenerated = { viewModel.setGeneratedCaptcha(it) })
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = viewModel.userCaptchaInput,
                        onValueChange = { viewModel.onCaptchaInputChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Código de imagen", color = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.finalizeLoginWithCaptcha() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Verificar")
                }
            },
            containerColor = Color.White
        )
    }

    if (authState is AuthState.LockedOut) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black).padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Timer, null, tint = Color.White, modifier = Modifier.size(80.dp))
                Spacer(Modifier.height(24.dp))
                Text("Seguridad Activada", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Demasiados intentos. Espera para reintentar:",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                val state = authState as AuthState.LockedOut
                Text(
                    text = String.format("%02d:%02d", (state.remainingTimeMs/1000)/60, (state.remainingTimeMs/1000)%60),
                    color = Color.White,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    AnimatedVisibility(
        visible = authState is AuthState.SuccessAnimation,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 20.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "¡Acceso Concedido!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Identidad verificada correctamente.\nCargando panel de gestión...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = Color.Black,
                        trackColor = Color.LightGray
                    )
                }
            }
        }
    }
}
