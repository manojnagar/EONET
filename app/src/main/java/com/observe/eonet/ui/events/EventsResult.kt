package com.observe.eonet.ui.events

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviResult

sealed class EventsResult : MviResult {

    sealed class LoadEventsResult : EventsResult() {
        object Loading : LoadEventsResult()
        data class Success(val events: List<EOEvent>) : LoadEventsResult()
        data class Failure(val error: Throwable) : LoadEventsResult()
    }

    //TODO: Result corresponding to every actions
    // Result -> When action performed on the data, it's output of action
}