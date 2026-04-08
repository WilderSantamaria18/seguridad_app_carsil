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
import androidx.compose.material.icons.filled.Inventory2
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

    LaunchedEffect(Unit) {
        viewModel.loadIntranetData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos", fontWeight = FontWeight.SemiBold) },
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
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nuevo producto", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = codigo,
                            onValueChange = { codigo = it },
                            label = { Text("Codigo") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripcion") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val compact = maxWidth < 520.dp
                            if (compact) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = marca,
                                        onValueChange = { marca = it },
                                        label = { Text("Marca") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = modelo,
                                        onValueChange = { modelo = it },
                                        label = { Text("Modelo") },
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
                                        value = marca,
                                        onValueChange = { marca = it },
                                        label = { Text("Marca") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = modelo,
                                        onValueChange = { modelo = it },
                                        label = { Text("Modelo") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val compact = maxWidth < 520.dp
                            if (compact) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = tipo,
                                        onValueChange = { tipo = it },
                                        label = { Text("Tipo") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = precio,
                                        onValueChange = { precio = it },
                                        label = { Text("Precio") },
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
                                        value = tipo,
                                        onValueChange = { tipo = it },
                                        label = { Text("Tipo") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = precio,
                                        onValueChange = { precio = it },
                                        label = { Text("Precio") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val compact = maxWidth < 520.dp
                            if (compact) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = stock,
                                        onValueChange = { stock = it },
                                        label = { Text("Stock") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = stockMinimo,
                                        onValueChange = { stockMinimo = it },
                                        label = { Text("Stock minimo") },
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
                                        value = stock,
                                        onValueChange = { stock = it },
                                        label = { Text("Stock") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = stockMinimo,
                                        onValueChange = { stockMinimo = it },
                                        label = { Text("Stock minimo") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.registrarProducto(
                                    codigo = codigo,
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    marca = marca,
                                    modelo = modelo,
                                    tipo = tipo,
                                    precioUnitario = precio,
                                    stock = stock,
                                    stockMinimo = stockMinimo
                                )
                                codigo = ""
                                nombre = ""
                                descripcion = ""
                                marca = ""
                                modelo = ""
                                tipo = ""
                                precio = ""
                                stock = ""
                                stockMinimo = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar producto")
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

            items(productos) { producto ->
                ProductoItem(producto)
            }
        }
    }
}

@Composable
private fun ProductoItem(producto: ProductoEntity) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                tint = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("${producto.Codigo} - ${producto.Nombre}", fontWeight = FontWeight.SemiBold)
                Text("S/ ${"%.2f".format(producto.PrecioUnitario)}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Stock: ${producto.Stock} | Min: ${producto.StockMinimo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (!producto.Marca.isNullOrBlank() || !producto.Modelo.isNullOrBlank()) {
                    Text(
                        "${producto.Marca ?: ""} ${producto.Modelo ?: ""}".trim(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
