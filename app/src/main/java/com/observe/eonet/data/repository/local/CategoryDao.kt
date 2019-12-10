package com.observe.eonet.data.repository.local

import androidx.room.*
import com.observe.eonet.data.model.db.DBCategory

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    fun getAll(): List<DBCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg category: DBCategory)

    @Query("DELETE FROM categories")
    fun deleteAll()

    @Delete
    fun delete(category: DBCategory)
}