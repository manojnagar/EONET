package com.observe.eonet.data.repository.remote

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.DataSource
import io.reactivex.Observable

class RemoteDataSource : DataSource {

    private val eonetApi by lazy {
        EONETApi.create()
    }

    override fun fetchCategory(): Observable<List<EOCategory>> {
        return eonetApi.fetchCategories()
            .map { response -> response.categories }
    }

    override fun fetchCategory(categoryId: String): Observable<EOCategory> {
        val openCategory = fetchCategory(categoryId, false)
        val closedCategory = fetchCategory(categoryId, true)
        return Observable.merge(openCategory, closedCategory)
    }

    private fun fetchCategory(categoryId: String, closed: Boolean): Observable<EOCategory> {
        val status = if (closed) "closed" else "open"
        return eonetApi
            .fetchCategory(categoryId, status)
    }

    override fun fetchEvents(category: EOCategory): Observable<List<EOEvent>> {
        return fetchCategory(categoryId = category.id)
            .map { response -> response.events }
    }

    override fun fetchEvents(): Observable<List<EOEvent>> {
        val openEvents = fetchEvents(forLastDays = 30, closed = false)
        val closedEvents = fetchEvents(forLastDays = 30, closed = true)

        //Merge both the events and return
        return Observable.merge(openEvents, closedEvents)
    }

    override fun fetchEvent(eventId: String): Observable<EOEvent> {
        return eonetApi.fetchEvent(eventId)
    }

    private fun fetchEvents(forLastDays: Int, closed: Boolean): Observable<List<EOEvent>> {
        val status = if (closed) "closed" else "open"
        return eonetApi
            .fetchEvents(forLastDays = forLastDays, status = status)
            .map { response -> response.events }
    }
}