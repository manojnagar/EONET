package com.observe.eonet.ui.category

import com.observe.eonet.mvibase.MviIntent

sealed class CategoriesIntent : MviIntent {

    object LoadCategoriesIntent : CategoriesIntent()

    object RetryLoadCategoriesIntent : CategoriesIntent()
}