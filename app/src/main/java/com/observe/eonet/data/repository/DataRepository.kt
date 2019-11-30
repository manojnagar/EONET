package com.observe.eonet.data.repository

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.remote.RemoteDataSource
import io.reactivex.Observable

class DataRepository : DataSource {

    private val remoteDataSource: DataSource by lazy {
        RemoteDataSource()
    }

    override fun fetchCategory(): Observable<List<EOCategory>> =
        remoteDataSource.fetchCategory()

    override fun fetchCategory(categoryId: String): Observable<EOCategory> =
        remoteDataSource.fetchCategory(categoryId)

    override fun fetchEvents(category: EOCategory): Observable<List<EOEvent>> =
        remoteDataSource.fetchEvents(category)

    override fun fetchEvents(): Observable<List<EOEvent>> = remoteDataSource.fetchEvents()

    override fun fetchEvent(eventId: String): Observable<EOEvent> =
        remoteDataSource.fetchEvent(eventId)
}