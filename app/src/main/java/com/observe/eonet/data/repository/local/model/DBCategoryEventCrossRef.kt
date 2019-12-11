package com.observe.eonet.data.repository.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["category_id", "event_id"])
data class DBCategoryEventCrossRef(
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "event_id") val eventId: String
)