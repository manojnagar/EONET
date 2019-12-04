package com.observe.eonet.ui.eventdetail

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviResult

sealed class EventDetailResult : MviResult {

    sealed class LoadEventDetailResult : EventDetailResult() {
        object Loading : LoadEventDetailResult()
        data class Success(val event: EOEvent) : LoadEventDetailResult()
        data class Failure(val error: Throwable) : LoadEventDetailResult()
    }

    object MapReadyResult : EventDetailResult()
}