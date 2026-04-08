package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.data.local.entity.ClienteEntity
import com.example.appcarsilauth.data.local.entity.ProductoEntity
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProformaScreen(
    viewModel: IntranetViewModel,
    idUsuario: Int,
    onBack: () -> Unit
) {
    val clientes by viewModel.clientes.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val proformaGenerada by viewModel.proformaGenerada.collectAsState()

    var selectedCliente by remember { mutableStateOf<ClienteEntity?>(null) }
    var selectedProducto by remember { mutableStateOf<ProductoEntity?>(null) }
    var cantidad by remember { mutableStateOf("") }
    
    var showClienteMenu by remember { mutableStateOf(false) }
    var showProductoMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadIntranetData()
        viewModel.resetState()
    }

    if (proformaGenerada) {
        val lastProforma by viewModel.lastProforma.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current

        AlertDialog(
            onDismissRequest = { viewModel.resetState(); onBack() },
            title = { Text("¡Proforma Registrada!", fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Los datos se han guardado en el servidor local (SQLite) y el stock ha sido actualizado.")
                    Spacer(Modifier.height(8.dp))
                    Text("Código: ${lastProforma?.Codigo}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (lastProforma != null && selectedCliente != null && selectedProducto != null) {
                            com.example.appcarsilauth.util.PdfGenerator.generateProformaPdf(
                                context = context,
                                proforma = lastProforma!!,
                                cliente = selectedCliente!!,
                                producto = selectedProducto!!,
                                cantidad = cantidad.toIntOrNull() ?: 0
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(Modifier.width(8.dp))
                    Text("DESCARGAR PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetState(); onBack() }) {
                    Text("VOLVER AL INICIO")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Proforma (Intranet)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C5364),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text("Seleccione Cliente del Sistema Web:", fontWeight = FontWeight.Bold, color = Color.Gray)
            ExposedDropdownMenuBox(
                expanded = showClienteMenu,
                onExpandedChange = { showClienteMenu = !showClienteMenu }
            ) {
                OutlinedTextField(
                    value = selectedCliente?.RazonSocial ?: "Selecciona...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClienteMenu) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showClienteMenu,
                    onDismissRequest = { showClienteMenu = false }
                ) {
                    clientes.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text(cliente.RazonSocial) },
                            onClick = {
                                selectedCliente = cliente
                                showClienteMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Añadir Producto:", fontWeight = FontWeight.Bold, color = Color.Gray)
            ExposedDropdownMenuBox(
                expanded = showProductoMenu,
                onExpandedChange = { showProductoMenu = !showProductoMenu }
            ) {
                OutlinedTextField(
                    value = selectedProducto?.Nombre ?: "Selecciona...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProductoMenu) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showProductoMenu,
                    onDismissRequest = { showProductoMenu = false }
                ) {
                    productos.forEach { producto ->
                        DropdownMenuItem(
                            text = { Text("${producto.Nombre} (S/ ${producto.PrecioUnitario})") },
                            onClick = {
                                selectedProducto = producto
                                showProductoMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = cantidad,
                onValueChange = { if(it.all { char -> char.isDigit() }) cantidad = it },
                label = { Text("Cantidad Numérica") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { 
                    val cant = cantidad.toIntOrNull() ?: 0
                    if (selectedCliente != null && selectedProducto != null && cant > 0) {
                        viewModel.generarProforma(
                            idUsuario = idUsuario,
                            idCliente = selectedCliente!!.IdCliente,
                            idProducto = selectedProducto!!.IdProducto,
                            cantidad = cant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                enabled = selectedCliente != null && selectedProducto != null && cantidad.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF0F2027))
                Spacer(Modifier.width(8.dp))
                Text("GENERAR ORDEN", fontWeight = FontWeight.Bold, color = Color(0xFF0F2027))
            }
            
            // Real-time calculation feedback
            val cantNum = cantidad.toIntOrNull() ?: 0
            val sub = (selectedProducto?.PrecioUnitario ?: 0.0) * cantNum
            val igv = sub * 0.18
            val total = sub + igv
            if (total > 0) {
                Spacer(Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Subtotal: S/ ${"%.2f".format(sub)}")
                        Text("IGV (18%): S/ ${"%.2f".format(igv)}")
                        Text("Total: S/ ${"%.2f".format(total)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}
