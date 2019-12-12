package com.observe.eonet.data.repository.remote

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import io.reactivex.Observable

interface EventApi {

    fun fetchEvents(category: EOCategory): Observable<List<EOEvent>>

    fun fetchEvents(): Observable<List<EOEvent>>

    fun fetchEvent(eventId: String): Observable<EOEvent>
}