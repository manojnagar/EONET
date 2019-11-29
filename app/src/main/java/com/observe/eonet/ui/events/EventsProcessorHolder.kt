package com.observe.eonet.ui.events

import com.observe.eonet.app.EONETApplication
import com.observe.eonet.ui.events.EventsAction.*
import com.observe.eonet.ui.events.EventsResult.*
import io.reactivex.Observable
import io.reactivex.ObservableTransformer

class EventsProcessorHolder {

    private val loadEventsProcessor =
        ObservableTransformer<LoadEventsAction, LoadEventsResult> { actions ->
            actions.flatMap { action ->
                EONETApplication.dataSource.fetchEvents()
                    .map { events -> LoadEventsResult.Success(events) }
                    .cast(LoadEventsResult::class.java)
                    .onErrorReturn(LoadEventsResult::Failure)
                    .subscribeOn(EONETApplication.schedulerProvider.io())
                    .observeOn(EONETApplication.schedulerProvider.ui())
                    .startWith(LoadEventsResult.Loading)
            }
        }

    private val selectEventProcessor =
        ObservableTransformer<SelectEventAction, SelectEventResult> { actions ->
            actions.map { SelectEventResult(it.event) }
        }

    private val detailPageOpenedProcessor =
        ObservableTransformer<DetailPageOpenedAction, DetailPageOpenedResult> { actions ->
            actions.map { DetailPageOpenedResult }
        }

    internal var actionProcessor =
        ObservableTransformer<EventsAction, EventsResult> { actions ->
            actions.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadEventsAction::class.java).compose(loadEventsProcessor),
                    shared.ofType(SelectEventAction::class.java).compose(selectEventProcessor),
                    shared.ofType(DetailPageOpenedAction::class.java).compose(
                        detailPageOpenedProcessor
                    )
                )
                    .cast(EventsResult::class.java)
                    .mergeWith(
                        // Error for not implemented actions
                        shared.filter { v ->
                            v !is LoadEventsAction &&
                                    v !is SelectEventAction &&
                                    v !is DetailPageOpenedAction
                        }.flatMap { w ->
                            Observable.error<EventsResult>(
                                IllegalArgumentException("Unknown Action type: $w")
                            )
                        }
                    )
            }
        }
}