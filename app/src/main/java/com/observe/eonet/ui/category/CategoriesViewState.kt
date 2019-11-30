package com.observe.eonet.ui.category

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.mvibase.MviViewState

data class CategoriesViewState(
    val isLoading: Boolean,
    val categories: List<EOCategory>,
    val error: Throwable?
) : MviViewState {
    companion object {
        fun idle(): CategoriesViewState {
            return CategoriesViewState(
                isLoading = false,
                categories = emptyList(),
                error = null
            )
        }
    }
}