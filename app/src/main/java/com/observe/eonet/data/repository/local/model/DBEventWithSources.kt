package com.observe.eonet.data.repository.local.model

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