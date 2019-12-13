package com.observe.eonet.data.repository

import android.util.Log
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.local.CategoryDao
import com.observe.eonet.data.repository.local.EventDao
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.remote.EventApi
import io.reactivex.Observable

class EventRepository(
    private val eventDao: EventDao,
    private val categoryDao: CategoryDao,
    private val eventApi: EventApi
) {

    fun getEvent(eventId: String): Observable<EOEvent> {
        return eventApi.fetchEvent(eventId)
            .doOnNext {
                Log.d(TAG, "Dispatching event from API... ${it.id}")
                storeEventsInDb(listOf(it))
            }
    }

    private fun getEventsFromApi(categoryId: String): Observable<List<EOEvent>> {
        return eventApi.fetchEvents()
            .doOnNext {
                Log.d(TAG, "Dispatching ${it.size} events from API...")
                storeEventsInDb(it)
            }
    }

    private fun getEventsFromApi(): Observable<List<EOEvent>> {
        return eventApi.fetchEvents()
            .doOnNext {
                Log.d(TAG, "Dispatching ${it.size} events from API...")
                storeEventsInDb(it)
            }
    }

    private fun storeEventsInDb(events: List<EOEvent>) {
        Log.d(TAG, "Inserting ${events.size} events from API in DB...")
        events.forEach { event ->
            eventDao.insert(event.convertToDBEvent())
            val categories = event.categories
                .map { it.convertToDBCategory() }
                .toTypedArray()
            categoryDao.insertAll(*categories)
            val crossRef = categories.map { category ->
                DBCategoryEventCrossRef(category.id, event.id)
            }.toTypedArray()
            eventDao.insertAllCategoryEventCrossRef(*crossRef)
        }
    }

    companion object {
        private val TAG = "EventRepo"
    }
}