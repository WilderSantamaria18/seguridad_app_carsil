package com.example.appcarsilauth.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.components.*
import com.example.appcarsilauth.ui.viewmodel.AuthState
import com.example.appcarsilauth.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarsilColors.Navy)
    ) {
        // Fondo con gradiente sutil para simular profundidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(CarsilColors.Accent.copy(alpha = 0.15f), Color.Transparent),
                        radius = 2000f
                    )
                )
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val isWide = maxWidth > 600.dp

            Card(
                modifier = Modifier
                    .widthIn(max = if (isWide) 1000.dp else 450.dp)
                    .heightIn(min = 600.dp)
                    .shadow(elevation = 40.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // PANEL IZQUIERDO (FORMULARIO)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 40.dp, vertical = 48.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Logo (Simulado si no existe R.drawable.carsil_logo)
                            Text(
                                text = "CARSIL",
                                style = LocalTextStyle.current.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    color = CarsilColors.Navy,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = "Equipos y Servicios",
                                fontSize = 12.sp,
                                color = CarsilColors.Gray400,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(48.dp))

                            Text(
                                text = "Bienvenido de vuelta",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B),
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Ingresa tus credenciales para acceder al sistema.",
                                fontSize = 14.sp,
                                color = CarsilColors.Gray400,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Error Alert Estilizado
                            if (authState is AuthState.Error) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    color = Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Error, null, tint = Color(0xFFB91C1C), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text((authState as AuthState.Error).message, color = Color(0xFFB91C1C), fontSize = 13.sp)
                                    }
                                }
                            }

                            // Inputs
                            // Inputs
                            Text("Correo electrónico", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            OutlinedTextField(
                                value = viewModel.email,
                                onValueChange = { viewModel.onEmailChange(it) },
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                placeholder = { Text("usuario@carsil.com", color = Color(0xFFCBD5E1)) },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = CarsilColors.Gray400, modifier = Modifier.size(18.dp)) },
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedBorderColor = CarsilColors.Primary,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Contraseña", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            OutlinedTextField(
                                value = viewModel.pin,
                                onValueChange = { viewModel.onPinChange(it) },
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                placeholder = { Text("••••••••", color = Color(0xFFCBD5E1)) },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = CarsilColors.Gray400, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = CarsilColors.Gray400)
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedBorderColor = CarsilColors.Primary,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // --- CAPTCHA SECTION (CARSIL-POL-ACC) ---
                            Text("Verificación de Seguridad", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            CaptchaComponent(onCaptchaGenerated = { viewModel.setGeneratedCaptcha(it) })
                            
                            OutlinedTextField(
                                value = viewModel.userCaptchaInput,
                                onValueChange = { viewModel.onCaptchaInputChange(it) },
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                placeholder = { Text("Introduce el código de arriba", color = Color(0xFFCBD5E1)) },
                                leadingIcon = { Icon(Icons.Default.Security, null, tint = CarsilColors.Gray400, modifier = Modifier.size(18.dp)) },
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedBorderColor = CarsilColors.Primary,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )
                            // ----------------------------------------

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botón con Gradiente Premium
                            Button(
                                onClick = { viewModel.attemptLogin(viewModel.email, viewModel.pin) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(CarsilColors.Primary, CarsilColors.Accent))
                                    ),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues()
                            ) {
                                if (authState is AuthState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ingresar al sistema", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }

                        Text(
                            text = "© 2026 CARSIL Equipos y Servicios. Todos los derechos reservados.",
                            fontSize = 11.sp,
                            color = CarsilColors.Gray400,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // PANEL DERECHO (DASHBOARD PREVIEW) - Solo en modo horizontal o ancho
                    if (isWide) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    Brush.linearGradient(listOf(CarsilColors.PanelBg, Color(0xFF162040)))
                                )
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            AIBadge()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Gestiona tu empresa\nde forma inteligente.",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = "Accede al dashboard para administrar clientes, ventas y proyectos con análisis de IA.",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
                            )
                            
                            MiniDashboardPreview()
                        }
                    }
                }
            }

            // Overlay de Bloqueo Temporal (CARSIL-POL-ACC)
            if (authState is AuthState.LockedOut) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Timer, null, tint = Color.White, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("ACCESO BLOQUEADO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "Intento bloqueado por 10 minutos (CARSIL-POL-ACC).",
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        val state = authState as AuthState.LockedOut
                        val min = (state.remainingTimeMs / 1000) / 60
                        val sec = (state.remainingTimeMs / 1000) % 60
                        Text(
                            text = String.format("%02d:%02d", min, sec),
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
