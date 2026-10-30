package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TipDao {
    @Query("SELECT * FROM tips WHERE category = :category AND status = 'PENDING'")
    fun getActiveTips(category: String): Flow<List<TipEntity>>

    @Query("SELECT * FROM tips WHERE category = :category AND status != 'PENDING'")
    fun getSettledTips(category: String): Flow<List<TipEntity>>

    @Query("SELECT * FROM tips WHERE category = :category")
    fun getAllTipsByCategory(category: String): Flow<List<TipEntity>>

    @Query("SELECT * FROM tips WHERE id = :id")
    suspend fun getTipById(id: Long): TipEntity?

    @Query("SELECT COUNT(*) FROM tips")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: TipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tips: List<TipEntity>)

    @Update
    suspend fun updateTip(tip: TipEntity)

    @Delete
    suspend fun deleteTip(tip: TipEntity)

    @Query("DELETE FROM tips")
    suspend fun clearAll()
}
