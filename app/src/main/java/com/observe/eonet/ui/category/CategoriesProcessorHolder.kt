package com.observe.eonet.ui.category

import com.observe.eonet.app.EONETApplication
import com.observe.eonet.ui.category.CategoriesAction.LoadCategoriesAction
import com.observe.eonet.ui.category.CategoriesResult.LoadCategoriesResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer

class CategoriesProcessorHolder {

    private val loadCategoryProcessorHolder =
        ObservableTransformer<LoadCategoriesAction, LoadCategoriesResult> { actions ->
            actions.flatMap { action ->
                EONETApplication.dataSource.fetchCategory()
                    .map { categories -> LoadCategoriesResult.Success(categories) }
                    .cast(LoadCategoriesResult::class.java)
                    .onErrorReturn(LoadCategoriesResult::Failure)
                    .subscribeOn(EONETApplication.schedulerProvider.io())
                    .observeOn(EONETApplication.schedulerProvider.ui())
                    .startWith(LoadCategoriesResult.Loading)
            }
        }

    internal var actionProcessor =
        ObservableTransformer<CategoriesAction, CategoriesResult> { actions ->
            actions.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadCategoriesAction::class.java).compose(
                        loadCategoryProcessorHolder
                    ),
                    Observable.empty()
                )
                    .cast(CategoriesResult::class.java)
                    .mergeWith(
                        // Error for not implemented actions
                        shared.filter { v ->
                            v !is LoadCategoriesAction
                        }.flatMap { w ->
                            Observable.error<LoadCategoriesResult>(
                                IllegalArgumentException("Unknown Action type: $w")
                            )
                        }
                    )
            }
        }
}