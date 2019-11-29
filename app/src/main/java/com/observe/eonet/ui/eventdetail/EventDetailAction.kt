package com.observe.eonet.ui.eventdetail

import com.observe.eonet.mvibase.MviAction

sealed class EventDetailAction : MviAction {

    data class LoadEventDetailAction(val eventId: String) : EventDetailAction()
}