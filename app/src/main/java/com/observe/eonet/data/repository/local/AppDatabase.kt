package com.observe.eonet.data.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.observe.eonet.data.model.db.DBCategory
import com.observe.eonet.data.model.db.DBEvent

@Database(entities = [DBEvent::class, DBCategory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun categoryDao(): CategoryDao
}