package com.observe.eonet.ui.eventdetail

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviViewState

sealed class EventDetailViewState : MviViewState {

    object LoadingView : EventDetailViewState()

    object EventNotExistView : EventDetailViewState()

    data class ErrorView(val message: String) : EventDetailViewState()

    data class DataView(val event: EOEvent) : EventDetailViewState()

    companion object {
        fun idle(): EventDetailViewState = LoadingView
    }
}