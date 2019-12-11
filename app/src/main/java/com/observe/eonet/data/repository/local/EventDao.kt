package com.observe.eonet.data.repository.local

import androidx.room.*
import com.observe.eonet.data.model.db.DBCategoryEventCrossRef
import com.observe.eonet.data.model.db.DBEvent
import com.observe.eonet.data.model.db.DBEventWithCategories
import com.observe.eonet.data.model.db.DBEventWithSources

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