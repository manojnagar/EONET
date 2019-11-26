package com.observe.eonet.data.repository.remote

import com.observe.eonet.data.model.EOEventResponse
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface EONETApi {

    @GET(EVENTS_ENDPOINT)
    fun fetchEvents(
        @Query("days") forLastDays: Int,
        @Query("status") status: String
    ): Observable<EOEventResponse>

    companion object {

        const val API = "https://eonet.sci.gsfc.nasa.gov/api/v2.1/"
        const val EVENTS_ENDPOINT = "events"

        fun create(): EONETApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(API)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(EONETApi::class.java)
        }
    }
}