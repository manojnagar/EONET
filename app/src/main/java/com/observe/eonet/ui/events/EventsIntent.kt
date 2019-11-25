package com.observe.eonet.ui.events

import com.observe.eonet.mvibase.MviIntent

sealed class EventsIntent : MviIntent {

    object LoadEventsIntent : EventsIntent()

    //TODO: All other actions for this screen from user end
    // Intent -> User/System/AnyComponent perform some operation on UI to perform a task
}