package com.observe.eonet.ui.eventdetail

import com.observe.eonet.mvibase.MviIntent

sealed class EventDetailIntent : MviIntent {

    object LoadEventDetailIntent : EventDetailIntent()
}