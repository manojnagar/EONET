package com.observe.eonet.data.repository.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "source", primaryKeys = ["associate_even_id", "link"])
data class DBSource(
    @ColumnInfo(name = "associate_even_id") val eventId: String,
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "link") val link: String
)