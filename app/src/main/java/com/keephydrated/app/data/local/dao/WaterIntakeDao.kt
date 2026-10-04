package com.keephydrated.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keephydrated.app.data.local.entity.WaterIntakeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WaterIntakeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: WaterIntakeEntity): Long

    @Query("DELETE FROM water_intakes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT * FROM water_intakes 
        WHERE timestamp >= :startTime AND timestamp <= :endTime 
        ORDER BY timestamp DESC
    """)
    fun getIntakesBetween(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<WaterIntakeEntity>>

    @Query("SELECT * FROM water_intakes ORDER BY timestamp DESC")
    fun getAllIntakes(): Flow<List<WaterIntakeEntity>>

    @Query("""
        SELECT * FROM water_intakes 
        WHERE timestamp >= :startTime AND timestamp <= :endTime 
        ORDER BY timestamp DESC LIMIT 1
    """)
    suspend fun getLatestIntakeBetween(startTime: LocalDateTime, endTime: LocalDateTime): WaterIntakeEntity?

    @Query("DELETE FROM water_intakes")
    suspend fun clearAll()
}
