package com.observe.eonet.ui.events

import com.observe.eonet.app.EONETApplication
import com.observe.eonet.ui.events.EventsAction.LoadEventsAction
import com.observe.eonet.ui.events.EventsResult.LoadEventsResult
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

    internal var actionProcessor =
        ObservableTransformer<EventsAction, EventsResult> { actions ->
            actions.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadEventsAction::class.java).compose(loadEventsProcessor),
                    Observable.empty()
                )
                    .cast(EventsResult::class.java)
                    .mergeWith(
                        // Error for not implemented actions
                        shared.filter { v ->
                            v !is LoadEventsAction
                        }.flatMap { w ->
                            Observable.error<EventsResult>(
                                IllegalArgumentException("Unknown Action type: $w")
                            )
                        }
                    )
            }
        }
}