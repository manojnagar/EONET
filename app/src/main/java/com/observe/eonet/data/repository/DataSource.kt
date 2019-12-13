package com.observe.eonet.data.repository

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.remote.CategoryApi
import com.observe.eonet.data.repository.remote.EventApi
import io.reactivex.Observable

/**
 * Contract for a database repository
 */
interface DataSource : EventApi, CategoryApi {

    fun fetchEvents(category: EOCategory): Observable<List<EOEvent>>
}