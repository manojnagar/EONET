package com.observe.eonet.data.repository

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.model.EOSource
import com.observe.eonet.data.repository.local.model.*
import com.observe.eonet.util.convertToDate
import com.observe.eonet.util.convertToString

fun EOEvent.convertToDBEvent(): DBEvent {
    return DBEvent(
        this.id, this.title, this.description,
        this.isClosed, this.startDate?.convertToString()
    )
}

fun EOCategory.convertToDBCategory(): DBCategory {
    return DBCategory(
        this.id,
        this.title,
        null,
        this.link
    )
}

fun EOSource.convertToDBSource(eventId: String): DBSource {
    return DBSource(
        eventId,
        this.id,
        this.url
    )
}

fun DBCategory.convertToEOCategory(): EOCategory {
    return EOCategory(
        this.id,
        this.title,
        this.link,
        mutableListOf()
    )
}

fun DBEvent.convertToEOEvent(): EOEvent {
    return EOEvent(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        categories = emptyList(),
        sources = emptyList(),
        geometries = emptyList(),
        isClosed = this.isClosed,
        startDate = this.startDate?.convertToDate()
    )
}

fun DBSource.convertToEOSource(): EOSource {
    return EOSource(this.id, this.link)
}

fun DBCategoryWithEvents.convertToEOCategory(): EOCategory {
    val eoEvents =
        this.events.map { it.convertToEOEvent() }.toMutableList()
    return this.category.convertToEOCategory().copy(events = eoEvents)
}

fun DBEventWithSources.convertToEOEvent(): EOEvent {
    val eoSources = this.sources.map { it.convertToEOSource() }
    return this.event.convertToEOEvent().copy(sources = eoSources)
}

fun DBEventWithCategories.convertToEOEvent(): EOEvent {
    val eoCategories = this.categories.map { it.convertToEOCategory() }
    return this.event.convertToEOEvent().copy(categories = eoCategories)
}
