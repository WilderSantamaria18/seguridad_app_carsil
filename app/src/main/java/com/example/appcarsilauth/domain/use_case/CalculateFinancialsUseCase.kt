package com.example.appcarsilauth.domain.use_case

import com.example.appcarsilauth.data.local.dao.SaleDao
import java.math.BigDecimal
import java.math.RoundingMode

class CalculateFinancialsUseCase(
    private val saleDao: SaleDao
) {
    /**
     * Calculates the total income strictly from "COMPLETADA" sales.
     * Ensures BigDecimal exact representation with 2 decimal points.
     */
    suspend fun getVerifiedTotalIncome(): BigDecimal {
        val totalString = saleDao.getTotalIncome()
        if (totalString.isNullOrBlank()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)
        }
        
        return try {
            val total = BigDecimal(totalString)
            total.setScale(2, RoundingMode.HALF_EVEN)
        } catch (e: Exception) {
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN)
        }
    }
}
