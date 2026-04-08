package com.example.appcarsilauth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_controls")
data class SecurityControl(
    @PrimaryKey val userId: String,
    val failedAttempts: Int = 0,
    val lastFailedAttemptTimestamp: Long = 0L,
    val lockoutUntil: Long = 0L
)
