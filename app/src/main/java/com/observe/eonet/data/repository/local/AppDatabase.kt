package com.observe.eonet.data.repository.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.observe.eonet.data.model.db.DBCategory
import com.observe.eonet.data.model.db.DBEvent

@Database(entities = [DBEvent::class, DBCategory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java,
                    "app_database"
                ).build()
            }
            return instance!!
        }

    }
}