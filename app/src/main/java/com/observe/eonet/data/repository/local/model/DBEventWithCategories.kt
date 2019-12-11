package com.observe.eonet.data.repository.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

class DBEventWithCategories(
    @Embedded val event: DBEvent,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "category_id",
        associateBy = Junction(DBCategoryEventCrossRef::class)
    )
    val categories: List<DBCategory>
)