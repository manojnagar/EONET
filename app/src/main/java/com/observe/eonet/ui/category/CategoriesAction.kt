package com.observe.eonet.ui.category

import com.observe.eonet.mvibase.MviAction

sealed class CategoriesAction : MviAction {

    object LoadCategoriesAction : CategoriesAction()
}