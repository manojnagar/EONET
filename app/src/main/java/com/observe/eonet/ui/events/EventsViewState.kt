package com.observe.eonet.ui.events

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviViewState

data class EventsViewState(
    val isLoading: Boolean,
    val isEventSelected: Boolean,
    val selectedEvent: EOEvent?,
    val events: List<EOEvent>,
    val error: Throwable?
) : MviViewState {

    companion object {
        fun idle(): EventsViewState = EventsViewState(
            isLoading = false,
            isEventSelected = false,
            selectedEvent = null,
            events = emptyList(),
            error = null
        )
    }
}