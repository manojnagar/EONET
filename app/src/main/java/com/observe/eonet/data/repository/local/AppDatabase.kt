package com.observe.eonet.data.repository.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.observe.eonet.data.repository.local.model.DBCategory
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.local.model.DBEvent
import com.observe.eonet.data.repository.local.model.DBSource

@Database(
    entities = [
        DBEvent::class,
        DBCategory::class,
        DBSource::class,
        DBCategoryEventCrossRef::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun categoryDao(): CategoryDao

    abstract fun sourceDao(): SourceDao

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