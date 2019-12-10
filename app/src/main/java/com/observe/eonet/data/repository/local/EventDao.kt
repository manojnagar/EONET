package com.observe.eonet.data.repository.local

import androidx.room.*
import com.observe.eonet.data.model.db.DBEvent
import com.observe.eonet.data.model.db.DBEventAndCategory

@Dao
interface EventDao {

    @Query("SELECT * FROM events")
    fun getAll(): List<DBEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg event: DBEvent)

    @Delete
    fun delete(event: DBEvent)

    @Query("DELETE FROM events")
    fun deleteAll()


    @Transaction
    @Query("SELECT * FROM categories")
    fun getEventsAndCategory(): List<DBEventAndCategory>
}