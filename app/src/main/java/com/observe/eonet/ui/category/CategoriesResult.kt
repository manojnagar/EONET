package com.observe.eonet.ui.category

import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.mvibase.MviResult

sealed class CategoriesResult : MviResult {

    sealed class LoadCategoriesResult : CategoriesResult() {
        object Loading : LoadCategoriesResult()
        data class Success(val categories: List<EOCategory>) : LoadCategoriesResult()
        data class Failure(val error: Throwable) : LoadCategoriesResult()
    }
}