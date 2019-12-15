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

    private val categoriesToUpdateTransformer =
        ObservableTransformer<List<EOCategory>, LoadCategoriesResult> { categories ->
            categories.map { LoadCategoriesResult.Update(it) }
                .cast(LoadCategoriesResult::class.java)
        }

    private fun downloadCategoryEvents(categories: List<EOCategory>): Observable<LoadCategoriesResult> {
        return categories
            .map { it.id }.distinct() // find different ids
            .toObservable()
            .flatMap({
                EONETApplication.categoryRepository.getCategory(it)
                    .subscribeOn(EONETApplication.schedulerProvider.io()) // Parallel execution
                    .onErrorResumeNext(Observable.empty<EOCategory>())
            }, 4)
            .scan(categories) { existingCategories, updatedCategory ->
                existingCategories.map { it.mergeEvents(updatedCategory) }
            }
            .compose(categoriesToUpdateTransformer)
    }

    private val loadCategoryProcessorHolder =
        ObservableTransformer<LoadCategoriesAction, LoadCategoriesResult> { actions ->
            actions.flatMap {
                EONETApplication.categoryRepository.getCategories()
                    .subscribeOn(EONETApplication.schedulerProvider.io())
                    .observeOn(EONETApplication.schedulerProvider.ui())
                    .doOnNext { Observable.just(it).compose(categoriesToUpdateTransformer) }
                    .lastElement().toObservable() // Convert last element to Observable
                    .switchMap { categories -> downloadCategoryEvents(categories) }
                    .startWith(LoadCategoriesResult.Loading)
                    .concatWith(Observable.just(LoadCategoriesResult.Complete))
                    .onErrorReturn(LoadCategoriesResult::Failure)
            }
        }

    internal var actionProcessor =
        ObservableTransformer<CategoriesAction, CategoriesResult> { actions ->
            actions.publish { shared ->
                shared.ofType(LoadCategoriesAction::class.java)
                    .compose(loadCategoryProcessorHolder)
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