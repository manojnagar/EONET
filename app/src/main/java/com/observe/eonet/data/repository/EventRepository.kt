package com.observe.eonet.data.repository

import android.util.Log
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.model.updateNonNullFields
import com.observe.eonet.data.repository.local.EventDao
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.remote.EventApi
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.Observables

class EventRepository(
    private val eventDao: EventDao,
    private val eventApi: EventApi
) {

    fun getEvent(eventId: String): Observable<EOEvent> {
        return getEventFromApi(eventId)
    }

    fun getEvents(fetchFromRemote: Boolean = true): Observable<List<EOEvent>> {
        Log.d(TAG, "Request received for getEvents")
        var dbReturnAnItem = false
        val dbObservable = getEventsFromDb()
            .doOnNext { dbReturnAnItem = true }
        val apiObservable = getEventsFromApi()
            .onErrorResumeNext(Function { error ->
                if (dbReturnAnItem) {
                    Observable.empty<List<EOEvent>>()
                } else {
                    Observable.error(error)
                }
            })
        return dbObservable
            .concatWith(if (fetchFromRemote) apiObservable else Observable.empty())
            .filter { it.isNotEmpty() }
    }

    private fun getEventsFromDb(): Observable<List<EOEvent>> {
        return Observables.zip(
            Observable.fromCallable {
                eventDao.getEventsWithSources().map { it.convertToEOEvent() }
            },
            Observable.fromCallable {
                eventDao.getEventsWithCategories().map { it.convertToEOEvent() }
            }) { withSources, withCategories ->
            withSources.map { eoEvent ->
                val categories = withCategories.first { it.id == eoEvent.id }.categories
                eoEvent.copy(categories = categories)
            }
        }.filter { it.isNotEmpty() }
    }

    private fun getEventFromDb(eventId: String): Observable<EOEvent> {
        return Observables.zip(
            eventDao.getEventWithSources(eventId)
                .toObservable()
                .map { it.convertToEOEvent() },
            eventDao.getEventWithCategories(eventId)
                .toObservable()
                .map { it.convertToEOEvent() })
        { withSources, withCategories ->
            withSources.copy(categories = withCategories.categories)
        }
    }

    private fun getEventFromApi(eventId: String): Observable<EOEvent> {
        return eventApi.fetchEvent(eventId)
            .map { it.copy(categories = it.categories.map(EOCategory::updateNonNullFields)) }
            .doOnNext {
                println("manoj event from server $it")
                Log.d(TAG, "Dispatching ${it.id} event from API...")
                storeEventsInDb(listOf(it))
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
            eventDao.insertCategories(categories)
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