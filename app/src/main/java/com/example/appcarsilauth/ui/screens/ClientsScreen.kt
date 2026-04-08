package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(Unit) {
        viewModel.loadIntranetData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atras")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nuevo cliente", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = documento,
                            onValueChange = { documento = it },
                            label = { Text("Documento") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = razonSocial,
                            onValueChange = { razonSocial = it },
                            label = { Text("Razon social") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Direccion") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val compact = maxWidth < 520.dp
                            if (compact) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = telefono,
                                        onValueChange = { telefono = it },
                                        label = { Text("Telefono") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = celular,
                                        onValueChange = { celular = it },
                                        label = { Text("Celular") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = telefono,
                                        onValueChange = { telefono = it },
                                        label = { Text("Telefono") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = celular,
                                        onValueChange = { celular = it },
                                        label = { Text("Celular") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = contacto,
                            onValueChange = { contacto = it },
                            label = { Text("Contacto") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.registrarCliente(
                                    documento = documento,
                                    razonSocial = razonSocial,
                                    direccion = direccion,
                                    telefono = telefono,
                                    celular = celular,
                                    email = email,
                                    contacto = contacto
                                )
                                documento = ""
                                razonSocial = ""
                                direccion = ""
                                telefono = ""
                                celular = ""
                                email = ""
                                contacto = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar cliente")
                        }

                        if (!uiMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiMessage ?: "",
                                color = Color(0xFF334155),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Divider()
                Spacer(modifier = Modifier.height(4.dp))
                Text("Listado", style = MaterialTheme.typography.titleMedium)
            }

            items(clientes) { cliente ->
                ClienteItem(cliente)
            }
        }
    }
}

@Composable
private fun ClienteItem(cliente: ClienteEntity) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(cliente.RazonSocial, fontWeight = FontWeight.SemiBold)
                Text("Doc: ${cliente.Documento}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (!cliente.Contacto.isNullOrBlank()) {
                    Text("Contacto: ${cliente.Contacto}", style = MaterialTheme.typography.bodySmall)
                }
                if (!cliente.Email.isNullOrBlank()) {
                    Text("Email: ${cliente.Email}", style = MaterialTheme.typography.bodySmall)
                }
                if (!cliente.Telefono.isNullOrBlank()) {
                    Text("Telefono: ${cliente.Telefono}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
