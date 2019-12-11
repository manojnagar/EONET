package com.observe.eonet.data.repository.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

class DBCategoryWithEvents(
    @Embedded val category: DBCategory,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "event_id",
        associateBy = Junction(DBCategoryEventCrossRef::class)
    )
    val events: List<DBEvent>
)