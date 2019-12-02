package com.observe.eonet.data.repository.remote

import com.google.gson.GsonBuilder
import com.observe.eonet.data.model.*
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EONETApi {

    @GET(CATEGORIES_ENDPOINT)
    fun fetchCategories(): Observable<EOCategoryResponse>

    @GET("$CATEGORIES_ENDPOINT/{categoryId}")
    fun fetchCategory(
        @Path("categoryId") categoryId: String,
        @Query("days") forLastDays: Int,
        @Query("status") status: String
    ): Observable<EOCategory>

    @GET(EVENTS_ENDPOINT)
    fun fetchEvents(
        @Query("days") forLastDays: Int,
        @Query("status") status: String
    ): Observable<EOEventResponse>

    @GET("$EVENTS_ENDPOINT/{eventId}")
    fun fetchEvent(@Path("eventId") eventId: String): Observable<EOEvent>

    companion object {

        const val API = "https://eonet.sci.gsfc.nasa.gov/api/v2.1/"
        const val EVENTS_ENDPOINT = "events"
        const val CATEGORIES_ENDPOINT = "categories"

        fun create(): EONETApi {
            val gson = GsonBuilder()
                .registerTypeAdapter(EOBaseGeometry::class.java, GeometryDeserializer())
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(API)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(EONETApi::class.java)
        }
    }
}