package com.observe.eonet.data.repository

import android.util.Log
import com.observe.eonet.app.EONETApplication
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.remote.RemoteDataSource
import io.reactivex.Observable

class DataRepository : DataSource {

    private val remoteDataSource: DataSource by lazy {
        RemoteDataSource()
    }

    override fun fetchCategory(): Observable<List<EOCategory>> {
        Log.e("manoj", "Testing code start")
        val categoryRepo = CategoryRepository(
            EONETApplication.appDatabase.categoryDao(),
            EONETApplication.appDatabase.eventDao(),
            remoteDataSource
        )
        categoryRepo.getCategories()
            .subscribeOn(EONETApplication.schedulerProvider.io())
            .observeOn(EONETApplication.schedulerProvider.ui())
            .subscribe {
                Log.e("manoj", "Result received ${it.size}")
                it.forEach { data -> println(data) }
            }

        Log.e("manoj", "Testing code end")
        return remoteDataSource.fetchCategory()
    }


    override fun fetchCategory(categoryId: String): Observable<EOCategory> =
        remoteDataSource.fetchCategory(categoryId)

    override fun fetchEvents(category: EOCategory): Observable<List<EOEvent>> =
        remoteDataSource.fetchEvents(category)

    override fun fetchEvents(): Observable<List<EOEvent>> = remoteDataSource.fetchEvents()

    override fun fetchEvent(eventId: String): Observable<EOEvent> =
        remoteDataSource.fetchEvent(eventId)
}