package com.observe.eonet.data.repository

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import io.reactivex.Observable

/**
 * Contract for a database repository
 */
interface DataSource {

    fun fetchCategory(): Observable<List<EOCategory>>

    fun fetchEvents(): Observable<List<EOEvent>>

    fun fetchEvent(eventId: String): Observable<EOEvent>
}