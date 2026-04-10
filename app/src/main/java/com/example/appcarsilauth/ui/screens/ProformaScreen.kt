package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
    val proformas by viewModel.proformas.collectAsState()
    val proformaGenerada by viewModel.proformaGenerada.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedCliente by remember { mutableStateOf<ClienteEntity?>(null) }
    var selectedProducto by remember { mutableStateOf<ProductoEntity?>(null) }
    var cantidad by remember { mutableStateOf("") }
    
    var showClienteMenu by remember { mutableStateOf(false) }
    var showProductoMenu by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab, searchQuery) {
        if (selectedTab == 1) {
            viewModel.loadAllProformas(searchQuery)
        } else {
            viewModel.loadIntranetData()
        }
    }

    if (proformaGenerada) {
        val lastProforma by viewModel.lastProforma.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current

        AlertDialog(
            onDismissRequest = { viewModel.resetState(); onBack() },
            title = { Text("Proforma Registrada", fontWeight = FontWeight.Black) },
            text = { 
                Column {
                    Text("Los datos se han guardado exitosamente en Railway.", fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Surface(color = Color(0xFFF1F3F4), shape = RoundedCornerShape(8.dp)) {
                        Text("Codigo: ${lastProforma?.Codigo}", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generar PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetState(); selectedTab = 1 }) {
                    Text("Ver Historial", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            Column(Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text("Módulo de Proformas", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.Black
                        )
                    }
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Nueva", modifier = Modifier.padding(16.dp), fontWeight = if(selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Historial", modifier = Modifier.padding(16.dp), fontWeight = if(selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    ) { padding ->
        if (selectedTab == 0) {
            // FORMULARIO DE CREACION (Igual que antes, simplificado)
            Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
                Text("Informacion del Cliente", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = showClienteMenu, onExpandedChange = { showClienteMenu = !showClienteMenu }) {
                    OutlinedTextField(
                        value = selectedCliente?.RazonSocial ?: "Seleccione un cliente...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClienteMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = showClienteMenu, onDismissRequest = { showClienteMenu = false }, modifier = Modifier.background(Color.White)) {
                        clientes.forEach { cliente ->
                            DropdownMenuItem(text = { Text(cliente.RazonSocial) }, onClick = { selectedCliente = cliente; showClienteMenu = false })
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Producto y Cantidad", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(expanded = showProductoMenu, onExpandedChange = { showProductoMenu = !showProductoMenu }) {
                    OutlinedTextField(
                        value = selectedProducto?.Nombre ?: "Seleccione...",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = showProductoMenu, onDismissRequest = { showProductoMenu = false }, modifier = Modifier.background(Color.White)) {
                        productos.forEach { producto ->
                            DropdownMenuItem(text = { Text("${producto.Nombre} (Stock: ${producto.Stock})") }, onClick = { selectedProducto = producto; showProductoMenu = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { if(it.all { c -> c.isDigit() }) cantidad = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { 
                        val cant = cantidad.toIntOrNull() ?: 0
                        if (selectedCliente != null && selectedProducto != null && cant > 0) {
                             viewModel.generarProforma(idUsuario, selectedCliente!!.IdCliente, selectedProducto!!.IdProducto, cant)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Generar Proforma", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // LISTADO DE PROFORMAS DESDE RAILWAY
            Column(modifier = Modifier.padding(padding)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Buscar proforma o cliente...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )

                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Text("Mostrando ${proformas.size} documentos en la nube", fontSize = 12.sp, color = Color.Gray) }
                    
                    items(proformas) { proforma ->
                        ProformaItem(proforma)
                    }
                }
            }
        }
    }
}

@Composable
fun ProformaItem(proforma: Map<String, Any>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(proforma["Codigo"] as String, fontWeight = FontWeight.Black, color = Color.Black)
                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                    Text(proforma["Estado"] as String, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }
            Text(proforma["Cliente"] as String, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(proforma["Fecha"] as String, fontSize = 11.sp, color = Color.Gray)
                Text("S/ ${"%.2f".format(proforma["Total"] as Double)}", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}
