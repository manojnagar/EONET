package com.observe.eonet.data.repository

import android.util.Log
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.local.CategoryDao
import com.observe.eonet.data.repository.local.EventDao
import com.observe.eonet.data.repository.local.model.DBCategory
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.local.model.DBCategoryWithEvents
import com.observe.eonet.data.repository.local.model.DBEvent
import com.observe.eonet.data.repository.remote.CategoryApi
import io.reactivex.Observable

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val eventDao: EventDao,
    private val categoryApi: CategoryApi
) {

    fun getCategories(): Observable<List<EOCategory>> {
        return Observable.concatArray(
            getCategoriesFromDb(),
            getCategoriesFromApi()
        )
    }

    private fun getCategoriesFromDb(): Observable<List<EOCategory>> {
        return Observable.fromCallable {
            categoryDao.getCategoryWithEvents()
                .map { convertToEOCategory(it) }
        }
            .doOnNext {
                Log.d(TAG, "Dispatching ${it.size} users from DB...")
            }
    }


    private fun getCategoriesFromApi(): Observable<List<EOCategory>> {
        return categoryApi.fetchCategory()
            .doOnNext {
                Log.d(TAG, "Dispatching ${it.size} categories from API...")
                storeCategoriesInDb(it)
            }
    }


    private fun storeCategoriesInDb(categories: List<EOCategory>) {
        Log.d(TAG, "Inserted ${categories.size} categories from API in DB...")
        categories.forEach { eoCategory ->
            categoryDao.insert(convertToDBCategory(eoCategory))
            val events = eoCategory.events?.map {
                convertToDBEvent(it)
            }?.toTypedArray() ?: emptyArray()
            eventDao.insertAll(*events)

            val crossRef =
                events.map { event ->
                    DBCategoryEventCrossRef(eoCategory.id, event.id)
                }.toTypedArray()
            categoryDao.insertAllCategoryEventCrossRef(*crossRef)
        }
    }

    private fun convertToEOCategory(dbCategoryWithEvents: DBCategoryWithEvents): EOCategory {
        val category = dbCategoryWithEvents.category
        val eoEvents = dbCategoryWithEvents.events.map {
            convertToEOEvent(it)
        }.toMutableList()

        return EOCategory(
            id = category.id,
            title = category.title,
            link = category.link,
            events = eoEvents
        )
    }

    private fun convertToEOEvent(dbEvent: DBEvent): EOEvent {
        return EOEvent(
            id = dbEvent.id,
            title = dbEvent.title,
            description = dbEvent.description ?: "",
            categories = emptyList(),
            sources = emptyList(),
            geometries = emptyList()
        )
    }

    private fun convertToDBCategory(category: EOCategory): DBCategory {
        return DBCategory(
            category.id,
            category.title,
            null,
            category.link
        )
    }

    private fun convertToDBEvent(event: EOEvent): DBEvent {
        return DBEvent(
            event.id,
            event.title,
            event.description
        )
    }

    companion object {
        private val TAG = "CategoryRepo"
    }
}