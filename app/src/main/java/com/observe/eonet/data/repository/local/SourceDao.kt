package com.observe.eonet.data.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.observe.eonet.data.repository.local.model.DBSource

@Dao
interface SourceDao {

    @Query("SELECT * FROM source")
    fun getAll(): List<DBSource>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg source: DBSource)

    @Query("DELETE FROM source")
    fun deleteAll()
}