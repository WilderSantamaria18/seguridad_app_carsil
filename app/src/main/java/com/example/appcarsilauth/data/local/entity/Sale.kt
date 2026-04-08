package com.example.appcarsilauth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey val id: String,
    val clientId: String,
    val amount: BigDecimal,
    val status: String, // e.g., "COMPLETADA", "ANULADA", "BORRADOR"
    val timestamp: Long = System.currentTimeMillis(),
    val vendorId: String
)
