package com.observe.eonet.data.repository.local

import androidx.room.*
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.local.model.DBEvent
import com.observe.eonet.data.repository.local.model.DBEventWithCategories
import com.observe.eonet.data.repository.local.model.DBEventWithSources

@Dao
interface EventDao {

    @Query("SELECT * FROM event")
    fun getAll(): List<DBEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg event: DBEvent)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllCategoryEventCrossRef(vararg categoryEventCrossRef: DBCategoryEventCrossRef)

    @Delete
    fun delete(event: DBEvent)

    @Query("DELETE FROM event")
    fun deleteAll()

    @Transaction
    @Query("SELECT * FROM event")
    fun getEventsWithSources(): List<DBEventWithSources>

    @Transaction
    @Query("SELECT * FROM event")
    fun getEventWithCategories(): List<DBEventWithCategories>
}