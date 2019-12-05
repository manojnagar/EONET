package com.observe.eonet.data.repository.local

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.DataSource
import io.reactivex.Observable

class LocalDataSource : DataSource {

    override fun fetchCategory(): Observable<List<EOCategory>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetchCategory(categoryId: String): Observable<EOCategory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetchEvents(category: EOCategory): Observable<List<EOEvent>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetchEvents(): Observable<List<EOEvent>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetchEvent(eventId: String): Observable<EOEvent> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}