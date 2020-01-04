package com.observe.eonet.data.repository.remote

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.DataSource
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables

class RemoteDataSource : DataSource {

    private val eonetApi by lazy {
        EONETApi.create()
    }

    override fun fetchCategory(): Observable<List<EOCategory>> {
        return eonetApi.fetchCategories()
            .map { response -> response.categories }
    }

    override fun fetchCategory(categoryId: String): Observable<EOCategory> {
        val openCategory = fetchCategory(categoryId, FOR_LAST_DAYS, false)
        val closedCategory = fetchCategory(categoryId, FOR_LAST_DAYS, true)
        return Observables.zip(openCategory, closedCategory) { first, second ->
            val combineEvents = mutableListOf<EOEvent>()
            first.events?.let {
                combineEvents.addAll(it)
            }
            second.events?.let {
                combineEvents.addAll(it)
            }
            first.copy(events = combineEvents)
        }
    }

    private fun fetchCategory(
        categoryId: String,
        forLastDays: Int,
        closed: Boolean
    ): Observable<EOCategory> {
        val status = if (closed) "closed" else "open"
        return eonetApi
            .fetchCategory(categoryId, forLastDays, status)
            .map {
                val events = it.events?.map { event ->
                    event.updateCloseStatusAndDate(closed)
                }?.toMutableList()
                it.copy(id = categoryId, events = events)
            }
    }

    override fun fetchEvents(category: EOCategory): Observable<List<EOEvent>> {
        return fetchCategory(categoryId = category.id)
            .map { response -> response.events }
    }

    override fun fetchEvents(): Observable<List<EOEvent>> {
        val openEvents = fetchEvents(forLastDays = FOR_LAST_DAYS, closed = false)
        val closedEvents = fetchEvents(forLastDays = FOR_LAST_DAYS, closed = true)

        //Merge both the events and return
        return Observables.zip(openEvents, closedEvents) { openEvents, closeEvents ->
            openEvents + closeEvents
        }.map { events ->
            events.map { event ->
                event.copy(categories = event.categories.map { eoCategory ->
                    eoCategory.copy(link = "")
                })
            }
        }
    }

    override fun fetchEvent(eventId: String): Observable<EOEvent> {
        return eonetApi.fetchEvent(eventId)
            .map { it.updateCloseStatusAndDate(false) }
    }

    private fun fetchEvents(forLastDays: Int, closed: Boolean): Observable<List<EOEvent>> {
        val status = if (closed) "closed" else "open"
        return eonetApi
            .fetchEvents(forLastDays = forLastDays, status = status)
            .map { response ->
                response.events.map { it.updateCloseStatusAndDate(closed) }
            }
    }

    private fun EOEvent.updateCloseStatusAndDate(isClosed: Boolean): EOEvent {
        val startDate = null
        return this.copy(isClosed = isClosed, startDate = startDate)
    }

    companion object {
        private const val FOR_LAST_DAYS = 30
    }
}