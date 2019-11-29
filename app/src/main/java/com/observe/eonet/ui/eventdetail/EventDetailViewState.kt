package com.observe.eonet.ui.eventdetail

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviViewState

data class EventDetailViewState(
    val isLoading: Boolean,
    val event: EOEvent?,
    val error: Throwable?
) : MviViewState {

    companion object {
        fun idle(): EventDetailViewState {
            return EventDetailViewState(
                isLoading = true,
                event = null,
                error = null
            )
        }
    }
}