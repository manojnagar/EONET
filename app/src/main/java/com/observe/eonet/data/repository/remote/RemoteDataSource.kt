package com.observe.eonet.data.repository.remote

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.DataSource
import io.reactivex.Observable

class RemoteDataSource : DataSource {

    private val eonetApi by lazy {
        EONETApi.create()
    }

    override fun fetchEvents(): Observable<List<EOEvent>> {
        val openEvents = fetchEvents(forLastDays = 360, closed = false)
        val closedEvents = fetchEvents(forLastDays = 360, closed = true)

        //Merge both the events and return
        return Observable.merge(openEvents, closedEvents)
    }

    private fun fetchEvents(forLastDays: Int, closed: Boolean): Observable<List<EOEvent>> {
        val status = if (closed) "closed" else "open"
        return eonetApi
            .fetchEvents(forLastDays = forLastDays, status = status)
            .map { response -> response.events }
    }
}