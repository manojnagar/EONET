package com.observe.eonet.data.model.db

import androidx.room.Embedded
import androidx.room.Relation

class DBEventWithSources(
    @Embedded val event: DBEvent,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "associate_even_id"
    )
    val sources: List<DBSource>
)