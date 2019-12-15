package com.observe.eonet.ui.category

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.mvibase.MviViewState


sealed class CategoriesViewState : MviViewState {

    object LoadingView : CategoriesViewState()

    object EmptyView : CategoriesViewState()

    data class ErrorView(val message: String) : CategoriesViewState()

    data class DataView(
        val isLoadingInProgress: Boolean,
        val categories: List<EOCategory>,
        val toastMessage: String?
    ) : CategoriesViewState()

    companion object {
        fun idle(): CategoriesViewState {
            return LoadingView
        }
    }
}