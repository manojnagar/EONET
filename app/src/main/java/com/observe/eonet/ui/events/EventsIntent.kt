package com.observe.eonet.ui.events

import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviIntent

sealed class EventsIntent : MviIntent {

    data class LoadEventsIntent(val categoryId: String?) : EventsIntent()

    data class SelectEventIntent(val event: EOEvent) : EventsIntent()

    object DetailPageOpenedIntent : EventsIntent()

    //TODO: All other actions for this screen from user end
    // Intent -> User/System/AnyComponent perform some operation on UI to perform a task
}