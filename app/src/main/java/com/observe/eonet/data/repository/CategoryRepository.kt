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
        return Observable.concatArray(
            getCategoriesFromDb(),
            getCategoriesFromApi()
        )
    }

    fun getCategory(categoryId: String): Observable<EOCategory> {
        return Observable.concatArray(
            getCategoryFromDB(categoryId),
            getCategoryFromApi(categoryId)
        )
    }

    fun getEvents(categoryId: String): Observable<List<EOEvent>> {
        return getCategory(categoryId).map { it.events }
    }

    private fun getCategoryFromDB(categoryId: String): Observable<EOCategory> {
        return Observable.create { categoryDao.get(categoryId) }
    }

    private fun getCategoriesFromDb(): Observable<List<EOCategory>> {
        return Observable.fromCallable {
            categoryDao.getCategoryWithEvents()
                .map { it.convertToEOCategory() }
        }
            .doOnNext {
                Log.d(TAG, "Dispatching ${it.size} categories from DB...")
            }
    }

    private fun getCategoryFromApi(categoryId: String): Observable<EOCategory> {
        return categoryApi.fetchCategory(categoryId)
            .doOnNext {
                Log.d(TAG, "Dispatching category from API... ${it.id}")
                storeCategoriesInDb(listOf(it))
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