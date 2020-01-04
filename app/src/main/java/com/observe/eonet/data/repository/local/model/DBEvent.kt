package com.observe.eonet.data.repository.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event")
data class DBEvent(
    @PrimaryKey @ColumnInfo(name = "event_id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "isClosed") val isClosed: Boolean?,
    @ColumnInfo(name = "start_date") val startDate: String?
)