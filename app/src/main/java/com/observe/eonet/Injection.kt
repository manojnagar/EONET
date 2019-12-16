package com.observe.eonet

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.observe.eonet.data.repository.CategoryRepository
import com.observe.eonet.data.repository.EventRepository
import com.observe.eonet.data.repository.local.AppDatabase
import com.observe.eonet.data.repository.remote.CategoryApi
import com.observe.eonet.data.repository.remote.EventApi
import com.observe.eonet.data.repository.remote.RemoteDataSource

object Injection {

    private val remoteDataSource by lazy {
        RemoteDataSource()
    }

    private fun provideCategoryApi(): CategoryApi = remoteDataSource

    private fun provideEventApi(): EventApi = remoteDataSource

    fun provideCategoryRepository(context: Context): CategoryRepository {
        val db = AppDatabase.getInstance(context)
        return CategoryRepository(db.categoryDao(), provideCategoryApi())
    }

    fun provideEventRepository(context: Context): EventRepository {
        val db = AppDatabase.getInstance(context)
        return EventRepository(db.eventDao(), provideEventApi())
    }

    fun provideFirebaseAnalytics(context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
}