package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.appcarsilauth.data.local.entity.ClienteEntity
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: IntranetViewModel,
    onBack: () -> Unit
) {
    val clientes by viewModel.clientes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var documento by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contacto by remember { mutableStateOf("") }
    
    var showAddForm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedClientDetail by remember { mutableStateOf<ClienteEntity?>(null) }

    LaunchedEffect(searchQuery) {
        viewModel.loadIntranetData(searchQuery)
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
                        Icon(if (showAddForm) Icons.Default.Close else Icons.Default.PersonAdd, null, tint = Color.Black)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = com.example.appcarsilauth.ui.components.CarsilColors.Primary,
                    trackColor = com.example.appcarsilauth.ui.components.CarsilColors.PrimaryLight
                )
            }
            if (showAddForm) {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Nuevo Registro en Railway", fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = documento, onValueChange = { documento = it }, label = { Text("RUC / DNI") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = razonSocial, onValueChange = { razonSocial = it }, label = { Text("Razón Social") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        Button(onClick = { showAddForm = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                            Text("Guardar Cliente")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = {
                    Text(
                        "Buscar cliente por documento...",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(25.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("Mostrando ${clientes.size} clientes", fontSize = 12.sp, color = Color.Black) }
                items(clientes) { cliente ->
                    ClienteCard(cliente) { selectedClientDetail = cliente }
                }
            }
        }

        // MODAL DE DETALLE DE CLIENTE
        if (selectedClientDetail != null) {
            ClientDetailModal(client = selectedClientDetail!!) { selectedClientDetail = null }
        }
    }
}

@Composable
fun ClienteCard(cliente: ClienteEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(45.dp).background(Color(0xFFF1F3F4), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Business, null, tint = Color.Black)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(cliente.RazonSocial, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text(cliente.Documento, fontSize = 12.sp, color = Color.Black)
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFFAFAFA))
            Spacer(Modifier.height(8.dp))
            InfoRow(Icons.Default.LocationOn, cliente.Direccion ?: "Ver más detalles...")
        }
    }
}

@Composable
private fun ClientDetailModal(client: ClienteEntity, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(60.dp).background(Color(0xFFF1F3F4), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(client.RazonSocial, fontWeight = FontWeight.Black, fontSize = 20.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("RUC: ${client.Documento}", color = Color.Black, fontSize = 14.sp)
                
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                DetailItem(Icons.Default.LocationOn, "Dirección Fiscal", client.Direccion ?: "No registrada")
                DetailItem(Icons.Default.Badge, "Contacto", client.Contacto ?: "Sin asignar")
                DetailItem(Icons.Default.Phone, "Teléfono / Celular", "${client.Telefono} / ${client.Celular}")
                DetailItem(Icons.Default.Email, "Correo Electrónico", client.Email ?: "Sin correo")

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Cerrar Detalle")
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = Color.Black)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = Color.Black, maxLines = 1)
    }
}
