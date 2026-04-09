package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.data.local.entity.ProductoEntity
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: IntranetViewModel,
    onBack: () -> Unit
) {
    val productos by viewModel.productos.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("") }
    
    var showAddForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadIntranetData()
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = { Text("Inventario de Productos", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = { showAddForm = !showAddForm }) {
                        Icon(if (showAddForm) Icons.Default.Close else Icons.Default.AddBox, null, tint = Color.Black)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (showAddForm) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Registrar Nuevo Activo/Producto", fontWeight = FontWeight.Bold)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = codigo,
                                onValueChange = { codigo = it },
                                label = { Text("Codigo SKU") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripcion Tecnica") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = marca,
                                onValueChange = { marca = it },
                                label = { Text("Marca") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = precio,
                                onValueChange = { precio = it },
                                label = { Text("Precio Venta") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = { Text("Stock inicial") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = stockMinimo,
                                onValueChange = { stockMinimo = it },
                                label = { Text("Stock Critico") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.registrarProducto(codigo, nombre, descripcion, marca, modelo, tipo, precio, stock, stockMinimo)
                                codigo = ""; nombre = ""; descripcion = ""; marca = ""; modelo = ""; tipo = ""; precio = ""; stock = ""; stockMinimo = ""
                                showAddForm = false
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Confirmar Ingreso", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Catálogo de Almacén (${productos.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(productos) { producto ->
                    ProductoCard(producto)
                }
            }
        }
    }
}

@Composable
private fun ProductoCard(producto: ProductoEntity) {
    val isLowStock = producto.Stock <= (producto.StockMinimo)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Inventory2, 
                            null, 
                            tint = if (isLowStock) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(producto.Nombre, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
                        Text("SKU: ${producto.Codigo}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                
                Text(
                    "S/ ${"%.2f".format(producto.PrecioUnitario)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF1F3F4))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Factory, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(producto.Marca ?: "Generico", fontSize = 12.sp, color = Color.Gray)
                }
                
                Surface(
                    color = if (isLowStock) Color(0xFFFFCDD2) else Color(0xFFC8E6C9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Stock: ${producto.Stock}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                    )
                }
            }
        }
    }
}
