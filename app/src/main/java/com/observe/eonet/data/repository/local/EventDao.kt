package com.observe.eonet.data.repository.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.observe.eonet.data.model.db.DBEvent

@Dao
interface EventDao {

    @Query("SELECT * FROM events")
    fun getAll(): List<DBEvent>

    @Insert
    fun insertAll(vararg event: DBEvent)

    @Delete
    fun delete(event: DBEvent)
}