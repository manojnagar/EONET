package com.observe.eonet

import android.content.Context
import com.observe.eonet.data.repository.CategoryRepository
import com.observe.eonet.data.repository.local.AppDatabase
import com.observe.eonet.data.repository.remote.CategoryApi
import com.observe.eonet.data.repository.remote.RemoteDataSource

object Injection {

    private val remoteDataSource by lazy {
        RemoteDataSource()
    }

    private fun provideCategoryApi(): CategoryApi = remoteDataSource

    fun provideCategoryRepository(context: Context): CategoryRepository {
        val db = AppDatabase.getInstance(context)
        return CategoryRepository(db.categoryDao(), provideCategoryApi())
    }
}