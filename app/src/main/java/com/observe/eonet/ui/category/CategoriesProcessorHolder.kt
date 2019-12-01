package com.observe.eonet.ui.category

import com.observe.eonet.app.EONETApplication
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.ui.category.CategoriesAction.LoadCategoriesAction
import com.observe.eonet.ui.category.CategoriesResult.LoadCategoriesResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject

class CategoriesProcessorHolder {

    private fun filterEventsForCategory(
        events: List<EOEvent>,
        category: EOCategory
    ): List<EOEvent> {
        return events.filter { event ->
            /*Category id should contain in events category events list and
            Event id should not contain in category events ids*/
            val isCategoryInEvent = event.categories.map { it.id }.contains(category.id)
            val isEventInCategory =
                category.events?.map { catEvent -> catEvent.id }?.contains(event.id)

            var result = isCategoryInEvent

            isEventInCategory?.let {
                result = result && (!it)
            }
            result
        }
    }

    private val downloadCategories =
        EONETApplication.dataSource.fetchCategory()

    private val downloadCategory = Observable.merge(
        downloadCategories.flatMap { categories ->
            categories.toObservable().map { category ->
                EONETApplication.dataSource.fetchCategory(category.id)
            }
        }, 5
    )

    private val updateCategories =
        downloadCategories.flatMap { categories ->
            downloadCategory.scan(categories) { existingCategories, updatedCategory ->
                existingCategories
                    .map { existingCategory ->
                        var eventsForCategory = emptyList<EOEvent>()
                        updatedCategory.events?.let {
                            eventsForCategory = filterEventsForCategory(it, existingCategory)
                        }

                        if (eventsForCategory.isNotEmpty()) {
                            val eventsList = mutableListOf<EOEvent>()
                            existingCategory.events?.let { eventsList.addAll(it) }
                            eventsList.addAll(eventsForCategory)
                            existingCategory.copy(events = eventsList)
                        } else {
                            existingCategory
                        }
                    }
            }
        }

    private val updateCompleteSubject: PublishSubject<LoadCategoriesResult> =
        PublishSubject.create()

    private val loadCategoryProcessorHolder =
        ObservableTransformer<LoadCategoriesAction, LoadCategoriesResult> { actions ->
            actions.flatMap {
                downloadCategories.concatWith(updateCategories)
                    .map { categories -> LoadCategoriesResult.Update(categories) }
                    .cast(LoadCategoriesResult::class.java)
                    .onErrorReturn(LoadCategoriesResult::Failure)
                    .subscribeOn(EONETApplication.schedulerProvider.io())
                    .observeOn(EONETApplication.schedulerProvider.ui())
                    .startWith(LoadCategoriesResult.Loading)
            }.doOnComplete {
                updateCompleteSubject.onNext(LoadCategoriesResult.Complete)
            }
        }

    internal var actionProcessor =
        ObservableTransformer<CategoriesAction, CategoriesResult> { actions ->
            actions.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadCategoriesAction::class.java).compose(
                        loadCategoryProcessorHolder
                    ),
                    updateCompleteSubject
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