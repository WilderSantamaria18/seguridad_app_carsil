package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.data.local.entity.ProductoEntity
import com.example.appcarsilauth.ui.components.CarsilColors
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: IntranetViewModel,
    onBack: () -> Unit
) {
    val productos by viewModel.productos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedProductDetail by remember { mutableStateOf<ProductoEntity?>(null) }

    LaunchedEffect(searchQuery) {
        viewModel.loadIntranetData(searchQuery)
    }

    LaunchedEffect(Unit) {
        viewModel.loadIntranetData()
    }

    Scaffold(
        containerColor = CarsilColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Inventario de Productos",
                        fontWeight = FontWeight.Black,
                        color = CarsilColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CarsilColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CarsilColors.Surface),
                actions = {
                    IconButton(onClick = { showAddForm = !showAddForm }) {
                        Icon(if (showAddForm) Icons.Default.Close else Icons.Default.AddBox, null, tint = CarsilColors.Primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = CarsilColors.Primary,
                    trackColor = CarsilColors.PrimaryLight
                )
            }
            if (showAddForm) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CarsilColors.Surface,
                    shadowElevation = 2.dp
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CarsilColors.Primary,
                                contentColor = CarsilColors.TextPrimary
                            )
                        ) {
                            Text("Confirmar Ingreso", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Barra de Búsqueda Dinámica de Inventario
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        "Buscar por Nombre o Código SKU...",
                        color = CarsilColors.TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = CarsilColors.Primary) },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CarsilColors.TextPrimary,
                    unfocusedTextColor = CarsilColors.TextPrimary,
                    focusedContainerColor = CarsilColors.Surface,
                    unfocusedContainerColor = CarsilColors.Surface,
                    focusedBorderColor = CarsilColors.Primary,
                    unfocusedBorderColor = CarsilColors.Stroke
                )
            )

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
                        color = CarsilColors.TextMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(productos) { producto ->
                    ProductoCard(producto) { selectedProductDetail = producto }
                }
            }
        }

        // MODAL DE DETALLE DE PRODUCTO
        if (selectedProductDetail != null) {
            ProductDetailModal(producto = selectedProductDetail!!) { selectedProductDetail = null }
        }
    }
}

@Composable
private fun ProductoCard(producto: ProductoEntity, onClick: () -> Unit) {
    val isLowStock = producto.Stock <= (producto.StockMinimo)
    val statusColor = if (isLowStock) Color(0xFFD32F2F) else Color(0xFF1B5E20)
    val statusBg = if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = CarsilColors.Surface,
        border = BorderStroke(1.dp, CarsilColors.Stroke),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono Identificador Minimalista
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Inventory2, 
                        null, 
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Información Principal (con control de peso para evitar overflow)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.Nombre, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = Color.Black,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SKU: ${producto.Codigo}", 
                        fontSize = 11.sp, 
                        color = CarsilColors.TextMuted,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Precio (Alineado a la derecha)
                Text(
                    text = "S/ ${"%.2f".format(producto.PrecioUnitario)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sección de detalles y Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Label, 
                        null, 
                        tint = CarsilColors.TextMuted, 
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = producto.Marca ?: "CARSIL GNR", 
                        fontSize = 12.sp, 
                        color = CarsilColors.TextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp)
                    )
                }
                
                // Badge de Stock Estilizado
                Surface(
                    color = if (isLowStock) Color(0xFFFFF1F0) else Color(0xFFF6FFED),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, if (isLowStock) Color(0xFFFFA39E) else Color(0xFFB7EB8F))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stock: ${producto.Stock}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductDetailModal(producto: ProductoEntity, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = CarsilColors.Surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(CarsilColors.PrimaryLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(32.dp), tint = CarsilColors.Primary)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = producto.Nombre,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Código SKU: ${producto.Codigo}",
                    color = CarsilColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(Icons.Default.Category, "Tipo de Activo", producto.Tipo ?: "General")
                DetailItem(Icons.Default.Label, "Marca / Fabricante", producto.Marca ?: "CARSIL SAC")
                DetailItem(Icons.Default.Settings, "Modelo", producto.Modelo ?: "Genérico")
                DetailItem(Icons.Default.Description, "Ficha Técnica", producto.Descripcion ?: "Sin descripción técnica adicional registrada.")
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailItemCompact(Icons.Default.Payments, "Precio Venta", "S/ ${"%.2f".format(producto.PrecioUnitario)}")
                    DetailItemCompact(Icons.Default.Warehouse, "Stock Actual", "${producto.Stock} Und")
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CarsilColors.Primary,
                        contentColor = CarsilColors.TextPrimary
                    )
                ) {
                    Text("Cerrar Detalle", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = CarsilColors.TextMuted, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 10.sp, color = CarsilColors.TextPrimary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = CarsilColors.TextPrimary)
        }
    }
}

@Composable
private fun DetailItemCompact(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = CarsilColors.TextMuted, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 10.sp, color = CarsilColors.TextPrimary, fontWeight = FontWeight.Bold)
        }
        Text(value, fontSize = 16.sp, color = CarsilColors.TextPrimary, fontWeight = FontWeight.Black)
    }
}
