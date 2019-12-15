package com.observe.eonet.data.repository

import android.util.Log
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.local.CategoryDao
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.remote.CategoryApi
import io.reactivex.Observable
import io.reactivex.functions.Function

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val categoryApi: CategoryApi
) {

    fun getCategories(): Observable<List<EOCategory>> {
        Log.d(TAG, "Request received for getCategories")
        val dbObservable = getCategoriesFromDb()
        val apiObservable = getCategoriesFromApi()
            .onErrorResumeNext(Function { error ->
                dbObservable.isEmpty
                    .toObservable()
                    .flatMap { empty: Boolean ->
                        if (empty) {
                            Observable.error(error)
                        } else {
                            Observable.empty<List<EOCategory>>()
                        }
                    }
            })

        return Observable.concatArray(
            dbObservable,
            apiObservable
        )
    }

    private fun getCategoriesFromDb(): Observable<List<EOCategory>> {
        return categoryDao.getCategoriesWithEvents()
            .toObservable()
            .filter { it.isNotEmpty() }
            .map { categories ->
                categories.map { it.convertToEOCategory() }
            }
            .doOnNext {
                Log.d(TAG, "Dispatching ${it.size} categories from DB...")
            }
    }

    private fun getCategoriesFromApi(): Observable<List<EOCategory>> {
        return categoryApi.fetchCategory()
            .doOnNext {
                Log.d(TAG, "Dispatching ${it.size} categories from API...")
                storeCategoriesInDb(it)
            }
    }

    fun getCategory(categoryId: String): Observable<EOCategory> {
        Log.d(TAG, "Request received for getCategory : $categoryId")
        val dbObservable = getCategoryFromDB(categoryId)
        val apiObservable = getCategoryFromApi(categoryId)
            .onErrorResumeNext(Function { error ->
                dbObservable.isEmpty
                    .toObservable()
                    .flatMap { empty: Boolean ->
                        if (empty) {
                            Observable.error(error)
                        } else {
                            Observable.empty<EOCategory>()
                        }
                    }
            })
        return dbObservable.concatWith(apiObservable)
    }

    fun getEvents(categoryId: String): Observable<List<EOEvent>> {
        Log.d(TAG, "Request received for getEvents : $categoryId")
        return getCategory(categoryId).map { category ->
            val result: List<EOEvent> = category.events ?: listOf()
            val copyCategory = category.copy(events = mutableListOf())
            result.map { event -> event.copy(categories = listOf(copyCategory)) }
        }
    }

    private fun getCategoryFromDB(categoryId: String): Observable<EOCategory> {
        Log.d(TAG, "Request received for category fetch from DB : $categoryId")
        return categoryDao.getCategoryWithEvents(categoryId)
            .map { it.convertToEOCategory() }
            .toObservable()
            .doOnNext {
                Log.d(TAG, "Dispatching ID: ${it.id} category from DB... ")
            }
    }

    private fun getCategoryFromApi(categoryId: String): Observable<EOCategory> {
        Log.d(TAG, "Request received for category fetch from API : $categoryId")
        return categoryApi
            .fetchCategory(categoryId)
            .map { it.copy(id = categoryId) }
            .doOnNext {
                Log.d(TAG, "Dispatching ID: ${it.id} category from API... ")
                storeCategoriesInDb(listOf(it))
            }
    }

    private fun storeCategoriesInDb(categories: List<EOCategory>) {
        Log.d(TAG, "Inserting ${categories.size} categories from API in DB...")
        categories.forEach { eoCategory ->
            categoryDao.insert(eoCategory.convertToDBCategory())
            val events = eoCategory.events?.map {
                it.convertToDBEvent()
            } ?: emptyList()
            categoryDao.insertEvents(events)

            val crossRef =
                events.map { event ->
                    DBCategoryEventCrossRef(eoCategory.id, event.id)
                }.toTypedArray()
            categoryDao.insertAllCategoryEventCrossRef(*crossRef)
        }
    }

    companion object {
        private val TAG = "CategoryRepo"
    }
}