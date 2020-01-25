package com.observe.eonet.ui.events

import com.observe.eonet.mvibase.MviAction

sealed class EventsAction : MviAction {

    data class LoadEventsAction(val categoryId: String?) : EventsAction()

    data class FilterEventsAction(val newText: String?): EventsAction()

    //TODO: Convert intent to action, multiple action can map to same action
    // Action -> Business logic rule which can be execute on the data
}