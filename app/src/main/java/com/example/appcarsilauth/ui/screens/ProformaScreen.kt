package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            title = { Text("Proforma Registrada", fontWeight = FontWeight.Black) },
            text = { 
                Column {
                    Text("Los datos se han guardado exitosamente. El stock ha sido actualizado en la base de datos local.", fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Surface(color = Color(0xFFF1F3F4), shape = RoundedCornerShape(8.dp)) {
                        Text("Codigo Operacion: ${lastProforma?.Codigo}", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (lastProforma != null && selectedCliente != null && selectedProducto != null) {
                            com.example.appcarsilauth.util.PdfGenerator.generateProformaPdf(
                                context = context, proforma = lastProforma!!, cliente = selectedCliente!!, producto = selectedProducto!!, cantidad = cantidad.toIntOrNull() ?: 0
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Generar PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetState(); onBack() }) {
                    Text("Finalizar", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = { Text("Generar Proforma", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Informacion del Cliente", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = showClienteMenu,
                onExpandedChange = { showClienteMenu = !showClienteMenu }
            ) {
                OutlinedTextField(
                    value = selectedCliente?.RazonSocial ?: "Seleccione un cliente...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClienteMenu) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black)
                )
                ExposedDropdownMenu(
                    expanded = showClienteMenu,
                    onDismissRequest = { showClienteMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    clientes.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text(cliente.RazonSocial, fontWeight = FontWeight.Medium) },
                            onClick = { selectedCliente = cliente; showClienteMenu = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Detalles de Venta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = showProductoMenu,
                        onExpandedChange = { showProductoMenu = !showProductoMenu }
                    ) {
                        OutlinedTextField(
                            value = selectedProducto?.Nombre ?: "Seleccione Producto...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Producto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProductoMenu) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = showProductoMenu,
                            onDismissRequest = { showProductoMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            productos.forEach { producto ->
                                DropdownMenuItem(
                                    text = { Text("${producto.Nombre} (P.U: S/ ${producto.PrecioUnitario})") },
                                    onClick = { selectedProducto = producto; showProductoMenu = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { if(it.all { char -> char.isDigit() }) cantidad = it },
                        label = { Text("Cantidad a Vender") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // RESUMEN DE TOTALES (ESTILO FACTURA)
            val cantNum = cantidad.toIntOrNull() ?: 0
            val sub = (selectedProducto?.PrecioUnitario ?: 0.0) * cantNum
            val igv = sub * 0.18
            val total = sub + igv

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.Black
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = Color.White.copy(alpha = 0.6f))
                        Text("S/ ${"%.2f".format(sub)}", color = Color.White)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("IGV (18%)", color = Color.White.copy(alpha = 0.6f))
                        Text("S/ ${"%.2f".format(igv)}", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("S/ ${"%.2f".format(total)}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { 
                    val cant = cantidad.toIntOrNull() ?: 0
                    if (selectedCliente != null && selectedProducto != null && cant > 0) {
                        viewModel.generarProforma(idUsuario, selectedCliente!!.IdCliente, selectedProducto!!.IdProducto, cant)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                enabled = selectedCliente != null && selectedProducto != null && cantidad.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, disabledContainerColor = Color.LightGray)
            ) {
                Icon(Icons.Default.Receipt, null)
                Spacer(Modifier.width(12.dp))
                Text("Confirmar Operacion", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}
