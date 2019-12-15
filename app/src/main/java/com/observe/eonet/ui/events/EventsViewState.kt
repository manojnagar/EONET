package com.observe.eonet.ui.events

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviViewState


sealed class EventsViewState : MviViewState {

    object LoadingView : EventsViewState()

    object EmptyView : EventsViewState()

    data class ErrorView(val message: String) : EventsViewState()

    data class DataView(
        val isLoadingInProgress: Boolean,
        val events: List<EOEvent>,
        val toastMessage: String?
    ) : EventsViewState()

    companion object {
        fun idle(): EventsViewState = LoadingView
    }
}