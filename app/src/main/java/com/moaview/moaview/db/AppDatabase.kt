package com.moaview.moaview.db

import androidx.room.*
import com.moaview.ViewerApplication
import com.moaview.moaview.dao.ContentsDao
import com.moaview.moaview.util.Converters

@Database(entities = [ContentsEntity::class],version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): ContentsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ViewerApplication.getApplicationContext(),
                    AppDatabase::class.java,
                    "database"
                )   .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}