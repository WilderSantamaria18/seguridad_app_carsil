package com.example.appcarsilauth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val action: String, // e.g., "CREATE_PROFORMA", "EDIT_SALE"
    val targetTable: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceMetadata: String // e.g., "Android API 34, Samsung S21"
)
