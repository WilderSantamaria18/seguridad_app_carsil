package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.data.local.entity.ClienteEntity
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: IntranetViewModel,
    onBack: () -> Unit
) {
    val clientes by viewModel.clientes.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    var documento by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contacto by remember { mutableStateOf("") }
    
    var showAddForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadIntranetData()
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = { Text("Gestion de Clientes", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = { showAddForm = !showAddForm }) {
                        Icon(if (showAddForm) Icons.Default.Close else Icons.Default.Add, null, tint = Color.Black)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Formulario Expandible para Nuevo Cliente
            if (showAddForm) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Registro de Nuevo Cliente", fontWeight = FontWeight.Bold, color = Color.Black)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = documento,
                                onValueChange = { documento = it },
                                label = { Text("Documento / RUC") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = razonSocial,
                                onValueChange = { razonSocial = it },
                                label = { Text("Razon Social") },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Direccion") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = telefono,
                                onValueChange = { telefono = it },
                                label = { Text("Telefono") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = celular,
                                onValueChange = { celular = it },
                                label = { Text("Celular") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Corporativo") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                viewModel.registrarCliente(documento, razonSocial, direccion, telefono, celular, email, contacto)
                                documento = ""; razonSocial = ""; direccion = ""; telefono = ""; celular = ""; email = ""; contacto = ""
                                showAddForm = false
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Guardar Cliente", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Listado con Estilo Corporativo
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Listado de Clientes (${clientes.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(clientes) { cliente ->
                    ClienteCard(cliente)
                }
            }
        }
    }
}

@Composable
private fun ClienteCard(cliente: ClienteEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F3F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(cliente.RazonSocial, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
                    Text(cliente.Documento, fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF1F3F4))
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(Icons.Default.LocationOn, cliente.Direccion ?: "No especificada")
            if (!cliente.Email.isNullOrBlank()) InfoRow(Icons.Default.Email, cliente.Email)
            if (!cliente.Telefono.isNullOrBlank()) InfoRow(Icons.Default.Phone, cliente.Telefono)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFF455A64))
    }
}
