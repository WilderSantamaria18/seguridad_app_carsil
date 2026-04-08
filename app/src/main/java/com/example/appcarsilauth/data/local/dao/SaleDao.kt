package com.example.appcarsilauth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appcarsilauth.data.local.entity.Sale

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale)

    @Query("SELECT * FROM sales WHERE status = 'COMPLETADA' ORDER BY timestamp DESC")
    suspend fun getCompletedSales(): List<Sale>

    @Query("SELECT SUM(amount) FROM sales WHERE status = 'COMPLETADA'")
    suspend fun getTotalIncome(): String? // String to maintain Precision via BigDecimal mapping

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    suspend fun getAllSales(): List<Sale>
}
