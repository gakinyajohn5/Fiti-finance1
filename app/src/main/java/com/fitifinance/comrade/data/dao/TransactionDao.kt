package com.fitifinance.comrade.data.dao

import androidx.room.*
import com.fitifinance.comrade.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE needsUserPrompt = 1 ORDER BY timestampMillis DESC")
    fun observePendingPrompts(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis DESC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    suspend fun getAllOnce(): List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(amountKes), 0) FROM transactions WHERE category = :category AND timestampMillis BETWEEN :startMillis AND :endMillis")
    suspend fun totalForCategoryBetween(category: String, startMillis: Long, endMillis: Long): Double

    /** One row per calendar day with any spend, for the daily spend chart. */
    @Query(
        """
        SELECT (timestampMillis / 86400000) AS dayBucket, SUM(amountKes) AS total
        FROM transactions
        WHERE timestampMillis BETWEEN :startMillis AND :endMillis
        GROUP BY dayBucket
        ORDER BY dayBucket ASC
        """
    )
    fun observeDailyTotalsBetween(startMillis: Long, endMillis: Long): Flow<List<DailySpendBucket>>
}

/** Epoch-day bucket (UTC) plus the total spent within it. */
data class DailySpendBucket(val dayBucket: Long, val total: Double)
