package com.example.appcarsilauth.data.repository

import com.example.appcarsilauth.data.local.dao.SecurityDao
import com.example.appcarsilauth.data.local.entity.SecurityControl
import com.example.appcarsilauth.domain.repository.SecurityRepository

class SecurityRepositoryImpl(
    private val securityDao: SecurityDao
) : SecurityRepository {

    override suspend fun getSecurityStatus(userId: String): SecurityControl? {
        return securityDao.getSecurityControl(userId)
    }

    override suspend fun recordFailedAttempt(userId: String) {
        val currentStatus = securityDao.getSecurityControl(userId)
        val timestamp = System.currentTimeMillis()
        
        if (currentStatus == null) {
            securityDao.insertSecurityControl(
                SecurityControl(userId = userId, failedAttempts = 1, lastFailedAttemptTimestamp = timestamp)
            )
        } else {
            securityDao.incrementFailedAttempts(userId, timestamp)
        }
    }

    override suspend fun resetSecurity(userId: String) {
        securityDao.resetSecurityControl(userId)
    }

    override suspend fun lockoutUser(userId: String, durationMillis: Long) {
        val currentStatus = securityDao.getSecurityControl(userId)
        val lockoutUntil = System.currentTimeMillis() + durationMillis
        
        if (currentStatus == null) {
            securityDao.insertSecurityControl(
                SecurityControl(userId = userId, failedAttempts = 3, lockoutUntil = lockoutUntil)
            )
        } else {
            securityDao.applyLockout(userId, lockoutUntil)
        }
    }
}
