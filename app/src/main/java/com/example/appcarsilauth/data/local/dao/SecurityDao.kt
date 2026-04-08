package com.example.appcarsilauth.data.local.dao

import androidx.room.*
import com.example.appcarsilauth.data.local.entity.SecurityControl

@Dao
interface SecurityDao {
    @Query("SELECT * FROM security_controls WHERE userId = :userId")
    suspend fun getSecurityControl(userId: String): SecurityControl?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityControl(control: SecurityControl)

    @Query("UPDATE security_controls SET failedAttempts = failedAttempts + 1, lastFailedAttemptTimestamp = :timestamp WHERE userId = :userId")
    suspend fun incrementFailedAttempts(userId: String, timestamp: Long)

    @Query("UPDATE security_controls SET failedAttempts = 0, lockoutUntil = 0 WHERE userId = :userId")
    suspend fun resetSecurityControl(userId: String)

    @Query("UPDATE security_controls SET lockoutUntil = :lockoutUntil WHERE userId = :userId")
    suspend fun applyLockout(userId: String, lockoutUntil: Long)
}
