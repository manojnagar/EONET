package com.observe.eonet.data.repository.local

import androidx.room.*
import com.observe.eonet.data.repository.local.model.DBCategory
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.local.model.DBCategoryWithEvents

@Dao
interface CategoryDao {

    @Query("SELECT * FROM category")
    fun getAll(): List<DBCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg category: DBCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllCategoryEventCrossRef(vararg categoryEventCrossRef: DBCategoryEventCrossRef)

    @Query("DELETE FROM category")
    fun deleteAll()

    @Delete
    fun delete(category: DBCategory)

    @Transaction
    @Query("SELECT * FROM category")
    fun getCategoryWithEvents(): List<DBCategoryWithEvents>
}