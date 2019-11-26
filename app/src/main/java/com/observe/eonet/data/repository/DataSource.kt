package com.observe.eonet.data.repository

import com.observe.eonet.data.model.EOEvent
import io.reactivex.Observable

/**
 * Contract for a database repository
 */
interface DataSource {

    fun fetchEvents(): Observable<List<EOEvent>>
}