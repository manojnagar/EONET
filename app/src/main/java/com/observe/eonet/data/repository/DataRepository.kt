package com.observe.eonet.data.repository

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.remote.RemoteDataSource
import io.reactivex.Observable

class DataRepository : DataSource {

    private val remoteDataSource: DataSource by lazy {
        RemoteDataSource()
    }

    override fun fetchCategory(): Observable<List<EOCategory>> = remoteDataSource.fetchCategory()

    override fun fetchEvents(): Observable<List<EOEvent>> = remoteDataSource.fetchEvents()

    override fun fetchEvent(eventId: String): Observable<EOEvent> =
        remoteDataSource.fetchEvent(eventId)
}