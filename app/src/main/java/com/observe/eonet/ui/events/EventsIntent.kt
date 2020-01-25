package com.observe.eonet.ui.events

import com.observe.eonet.mvibase.MviIntent

sealed class EventsIntent : MviIntent {

    data class LoadEventsIntent(val categoryId: String?) : EventsIntent()

    object PullToRefreshIntent : EventsIntent()

    object RetryLoadEventIntent : EventsIntent()

    data class UserQueryChangeIntent(val newText: String?) : EventsIntent()

    //TODO: All other actions for this screen from user end
    // Intent -> User/System/AnyComponent perform some operation on UI to perform a task
}