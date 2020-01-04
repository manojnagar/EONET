package com.observe.eonet.data.model

data class EOCategory(
    val id: String,
    val title: String,
    val link: String,
    val events: MutableList<EOEvent>?
)

fun EOCategory.updateNonNullFields(): EOCategory {
    //In network requests some fields are missing
    return this.copy(title = this.title ?: "", link = this.link ?: "")
}

fun List<EOEvent>.filterEvents(category: EOCategory): List<EOEvent> {
    if (this.isEmpty()) {
        return this
    }

    return this.filter { event ->
        /*Category id should contain in events category events list and
        Event id should not contain in category events ids*/
        val isCategoryInEvent = event.categories.map { it.id }.contains(category.id)
        val isEventInCategory =
            category.events?.map { catEvent -> catEvent.id }?.contains(event.id) ?: false
        isCategoryInEvent && !isEventInCategory
    }
}

fun EOCategory.mergeEvents(events: List<EOEvent>): EOCategory {
    val mergedEvents = mutableListOf<EOEvent>()
    this.events?.let {
        mergedEvents.addAll(it)
    }
    val filteredEvents = events.filter { event ->
        /*Category id should contain in events category events list and
        Event id should not contain in category events ids*/
        val isCategoryInEvent = event.categories.map { it.id }.contains(this.id)
        val isEventInCategory = mergedEvents.map { catEvent -> catEvent.id }.contains(event.id)
        isCategoryInEvent && !isEventInCategory
    }
    mergedEvents.addAll(filteredEvents)
    return this.copy(events = mergedEvents)
}