package com.observe.eonet.ui.eventdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EventDetailViewModelFactory(private val eventId: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //return EventDetailViewModel(eventId) as T
        return EventDetailViewModel() as T
    }

}