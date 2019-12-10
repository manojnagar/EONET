package com.observe.eonet.data.model.db

import androidx.room.Embedded
import androidx.room.Relation

data class DBEventAndCategory(
    @Embedded
    val category: DBCategory,
    @Relation(
        parentColumn = "server_id",
        entityColumn = "category_id"
    )
    val event: DBEvent

)