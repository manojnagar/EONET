package com.observe.eonet.data.repository

import android.util.Log
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.data.repository.local.CategoryDao
import com.observe.eonet.data.repository.local.model.DBCategoryEventCrossRef
import com.observe.eonet.data.repository.remote.CategoryApi
import io.reactivex.Observable

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val categoryApi: CategoryApi
) {

    fun getCategories(): Observable<List<EOCategory>> {
        Log.d(TAG, "Request received for getCategories")
        return Observable.concatArray(
            getCategoriesFromDb(),
            getCategoriesFromApi()
        )
    }

    fun getEvents(categoryId: String): Observable<List<EOEvent>> {
        Log.d(TAG, "Request received for getEvents : $categoryId")
        return getCategory(categoryId).map { it.events }
    }

    private fun getCategoriesFromDb(): Observable<List<EOCategory>> {
        return categoryDao.getCategoryWithEvents()
            .toObservable()
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
        return Observable.merge(
            getCategoryFromDB(categoryId),
            getCategoryFromApi(categoryId)
        )
    }

    private fun getCategoryFromDB(categoryId: String): Observable<EOCategory> {
        Log.d(TAG, "Request received for category fetch from DB : $categoryId")
        return categoryDao.get(categoryId)
            .map { it.convertToEOCategory() }
            .toObservable()
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