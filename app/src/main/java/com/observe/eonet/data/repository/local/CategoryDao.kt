package com.observe.eonet.data.repository.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.observe.eonet.data.model.db.DBCategory

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    fun getAll(): List<DBCategory>

    @Insert
    fun insertAll(vararg category: DBCategory)

    @Delete
    fun delete(category: DBCategory)
}