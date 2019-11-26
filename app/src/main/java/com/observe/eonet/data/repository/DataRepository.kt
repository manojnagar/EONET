package com.observe.eonet.data.repository

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.remote.RemoteDataSource
import io.reactivex.Observable

class DataRepository : DataSource {

    private val remoteDataSource: RemoteDataSource by lazy {
        RemoteDataSource()
    }

    override fun fetchEvents(): Observable<List<EOEvent>> = remoteDataSource.fetchEvents()
}