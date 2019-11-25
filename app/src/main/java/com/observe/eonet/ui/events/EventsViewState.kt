package com.observe.eonet.ui.events

import com.observe.eonet.data.model.Event
import com.observe.eonet.mvibase.MviViewState

data class EventsViewState(
    val isLoading: Boolean,
    val events: List<Event>,
    val error: Throwable?
) : MviViewState {

    companion object {
        fun idle(): EventsViewState = EventsViewState(
            isLoading = false,
            events = emptyList(),
            error = null
        )
    }
}