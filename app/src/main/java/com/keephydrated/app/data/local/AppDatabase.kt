package com.keephydrated.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.keephydrated.app.data.local.converter.DateTimeConverters
import com.keephydrated.app.data.local.dao.WaterIntakeDao
import com.keephydrated.app.data.local.entity.WaterIntakeEntity

@Database(
    entities = [WaterIntakeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val waterIntakeDao: WaterIntakeDao

    companion object {
        const val DATABASE_NAME = "keep_hydrated_db"
    }
}
