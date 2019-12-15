package com.observe.eonet.ui.category

import com.observe.eonet.app.EONETApplication
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.mergeEvents
import com.observe.eonet.ui.category.CategoriesAction.LoadCategoriesAction
import com.observe.eonet.ui.category.CategoriesResult.LoadCategoriesResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.toObservable

class CategoriesProcessorHolder {

    private val loadCategoryProcessorHolder =
        ObservableTransformer<LoadCategoriesAction, LoadCategoriesResult> { actions ->
            actions.flatMap {
                EONETApplication.categoryRepository.getCategories()
                    .subscribeOn(EONETApplication.schedulerProvider.io())
                    .observeOn(EONETApplication.schedulerProvider.ui())
                    .doOnNext { categories ->
                        Observable.just(LoadCategoriesResult.Update(categories))
                            .cast(LoadCategoriesResult::class.java)
                    }
                    .lastElement()
                    .toObservable()
                    .switchMap { categories ->
                        categories.toObservable()
                            .map { category -> category.id }
                            .distinct()
                            .subscribeOn(EONETApplication.schedulerProvider.io())
                            .flatMap({ categoryId ->
                                EONETApplication.categoryRepository.getCategory(categoryId)
                                    .onErrorResumeNext(Observable.empty<EOCategory>())
                            }, 5)
                            .scan(categories) { existingCategories, updatedCategory ->
                                existingCategories
                                    .map { existingCategory ->
                                        existingCategory.mergeEvents(updatedCategory)
                                    }
                            }
                            .map { LoadCategoriesResult.Update(it) }
                            .cast(LoadCategoriesResult::class.java)
                    }
                    .startWith(LoadCategoriesResult.Loading)
                    .onErrorReturn(LoadCategoriesResult::Failure)
                    .concatWith(Observable.just(LoadCategoriesResult.Complete))
            }
        }

    internal var actionProcessor =
        ObservableTransformer<CategoriesAction, CategoriesResult> { actions ->
            actions.publish { shared ->
                    shared.ofType(LoadCategoriesAction::class.java).compose(
                        loadCategoryProcessorHolder
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