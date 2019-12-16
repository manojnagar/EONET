package com.observe.eonet.firebase

import android.os.Bundle
import com.observe.eonet.app.EONETApplication

object AnalyticsManager {

    private const val ScreenView = "screen_open"
    private const val paramName = "name"

    fun reportScreenViewEvent(name: String, extras: Map<String, String> = mapOf()) {
        val params = Bundle()
        params.putString(paramName, name)

        var filteredExtras = extras.filterValues { it.isNotEmpty() }
        filteredExtras = filteredExtras.filterKeys { it.isNotEmpty() }
        for ((key, value) in filteredExtras) {
            params.putString(key, value)
        }
        logEvent(ScreenView, params)
    }

    private fun logEvent(name: String, params: Bundle) {
        EONETApplication.firebaseAnalytics.logEvent(name, params)
    }
}