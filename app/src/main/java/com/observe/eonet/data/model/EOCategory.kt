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

fun EOCategory.mergeEvents(category: EOCategory): EOCategory {
    //Check validity
    if (this.id != (category.id)) {
        return this
    }


    val mergedEvents = mutableListOf<EOEvent>()
    this.events?.let {
        mergedEvents.addAll(it)
    }
    val filteredEvents = category.events?.filter { event ->
        /*Category id should contain in events category events list and
        Event id should not contain in category events ids*/
        val isCategoryInEvent = event.categories.map { it.id }.contains(this.id)
        val isEventInCategory = mergedEvents.map { catEvent -> catEvent.id }.contains(event.id)
        isCategoryInEvent && !isEventInCategory
    }
    filteredEvents?.let { mergedEvents.addAll(it) }
    return this.copy(events = mergedEvents)
}