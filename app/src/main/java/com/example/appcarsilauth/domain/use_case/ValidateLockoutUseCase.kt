package com.example.appcarsilauth.domain.use_case

import com.example.appcarsilauth.domain.repository.SecurityRepository

class ValidateLockoutUseCase(
    private val securityRepository: SecurityRepository
) {
    companion object {
        const val MAX_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutos
    }

    suspend fun isLockedOut(userId: String): Boolean {
        val status = securityRepository.getSecurityStatus(userId) ?: return false
        val currentTime = System.currentTimeMillis()
        
        return if (status.lockoutUntil > currentTime) {
            true // Still locked out
        } else {
            // Lockout expired, reset security parameters natively
            if (status.lockoutUntil > 0) {
                securityRepository.resetSecurity(userId)
            }
            false
        }
    }

    suspend fun recordFailedLogin(userId: String): Boolean {
        securityRepository.recordFailedAttempt(userId)
        val status = securityRepository.getSecurityStatus(userId)
        
        if (status != null && status.failedAttempts >= MAX_ATTEMPTS) {
            securityRepository.lockoutUser(userId, LOCKOUT_DURATION_MS)
            return true // Triggered Lockout
        }
        return false // Not locked out yet
    }

    suspend fun onSuccessfulLogin(userId: String) {
        securityRepository.resetSecurity(userId)
    }
    
    suspend fun getRemainingLockoutTimeMs(userId: String): Long {
        val status = securityRepository.getSecurityStatus(userId) ?: return 0L
        val remaining = status.lockoutUntil - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }

    /** Cuántos intentos le quedan antes de ser bloqueado. */
    suspend fun getRemainingAttempts(userId: String): Int {
        val status = securityRepository.getSecurityStatus(userId) ?: return MAX_ATTEMPTS
        val used = status.failedAttempts.coerceAtLeast(0)
        return (MAX_ATTEMPTS - used).coerceAtLeast(0)
    }
}
