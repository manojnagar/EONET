package com.observe.eonet.data.repository

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.local.model.DBCategory
import com.observe.eonet.data.repository.local.model.DBCategoryWithEvents
import com.observe.eonet.data.repository.local.model.DBEvent

fun EOEvent.convertToDBEvent(): DBEvent {
    return DBEvent(this.id, this.title, this.description)
}

fun EOCategory.convertToDBCategory(): DBCategory {
    return DBCategory(
        this.id,
        this.title,
        null,
        this.link
    )
}

fun DBEvent.convertToEOEvent(): EOEvent {
    return EOEvent(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        categories = emptyList(),
        sources = emptyList(),
        geometries = emptyList()
    )
}

fun DBCategoryWithEvents.convertToEOCategory(): EOCategory {
    val category = this.category
    val eoEvents =
        this.events
            .map { it.convertToEOEvent() }
            .toMutableList()
    return EOCategory(
        id = category.id,
        title = category.title,
        link = category.link,
        events = eoEvents
    )
}
