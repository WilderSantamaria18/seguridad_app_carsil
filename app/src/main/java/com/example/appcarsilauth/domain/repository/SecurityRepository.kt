package com.example.appcarsilauth.domain.repository

import com.example.appcarsilauth.data.local.entity.SecurityControl

interface SecurityRepository {
    suspend fun getSecurityStatus(userId: String): SecurityControl?
    suspend fun recordFailedAttempt(userId: String)
    suspend fun resetSecurity(userId: String)
    suspend fun lockoutUser(userId: String, durationMillis: Long)
}
