package com.observe.eonet.ui.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FilterViewModel : ViewModel() {
    private val filterText = MutableLiveData<String>()

    fun setFilterText(text: String) {
        filterText.value = text
    }

    fun getFilterText(): LiveData<String> {
        return filterText
    }
}
