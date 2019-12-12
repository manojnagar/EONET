package com.observe.eonet.data.repository.remote

import com.observe.eonet.data.model.EOCategory
import io.reactivex.Observable

interface CategoryApi {

    fun fetchCategory(): Observable<List<EOCategory>>

    fun fetchCategory(categoryId: String): Observable<EOCategory>
}