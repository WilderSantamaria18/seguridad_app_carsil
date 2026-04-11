package com.example.appcarsilauth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarsilauth.ui.viewmodel.IntranetViewModel
import com.example.appcarsilauth.data.local.entity.ProductoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: IntranetViewModel,
    onBack: () -> Unit
) {
    val productos by viewModel.productos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadIntranetData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario de Bombas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Resumen de Stock
            InventorySummaryCard(productos)

            Text(
                text = "LISTADO DE STOCK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos) { producto ->
                    ProductStockItem(producto)
                }
            }
        }
    }
}

@Composable
fun InventorySummaryCard(productos: List<ProductoEntity>) {
    val totalStock = productos.sumOf { it.Stock }
    val lowStockCount = productos.count { it.Stock < 10 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Inventory,
                contentDescription = null,
                tint = Color(0xFF3B49DF),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Total de Bombas en Almacén", color = Color.Black, fontSize = 12.sp)
                Text("$totalStock Unidades", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (lowStockCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$lowStockCount productos en nivel crítico", color = Color.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductStockItem(producto: ProductoEntity) {
    val statusColor = when {
        producto.Stock == 0 -> Color(0xFFEF4444) // Rojo (Agotado)
        producto.Stock < 10 -> Color(0xFFF59E0B) // Ámbar (Bajo)
        else -> Color(0xFF10B981) // Verde (OK)
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.Nombre, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("ID: ${producto.IdProducto} | S/ ${producto.PrecioUnitario}", fontSize = 12.sp, color = Color.Black)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${producto.Stock}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = when {
                        producto.Stock == 0 -> "AGOTADO"
                        producto.Stock < 10 -> "BAJO"
                        else -> "STOCK OK"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
