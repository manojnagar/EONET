package com.observe.eonet.ui.events

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviAction

sealed class EventsAction : MviAction {

    data class LoadEventsAction(val categoryId: String?) : EventsAction()

    data class SelectEventAction(val event: EOEvent) : EventsAction()

    object DetailPageOpenedAction : EventsAction()

    //TODO: Convert intent to action, multiple action can map to same action
    // Action -> Business logic rule which can be execute on the data
}