package com.observe.eonet.ui.events

import com.observe.eonet.app.EONETApplication
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.ui.events.EventsAction.LoadEventsAction
import com.observe.eonet.ui.events.EventsResult.LoadEventsResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer

class EventsProcessorHolder {

    private fun getEventsForCategory(categoryId: String): Observable<List<EOEvent>> {
        return EONETApplication.categoryRepository.getEvents(categoryId)
    }

    private fun getEvents(): Observable<List<EOEvent>> {
        return EONETApplication.eventRepository.getEvents()
    }

    private fun getEvents(categoryId: String?): Observable<List<EOEvent>> {
        return if (categoryId != null) getEventsForCategory(categoryId) else getEvents()
    }

    private val loadEventsProcessor =
        ObservableTransformer<LoadEventsAction, LoadEventsResult> { actions ->
            actions.flatMap { action ->
                getEvents(action.categoryId)
                    .subscribeOn(EONETApplication.schedulerProvider.io())
                    .observeOn(EONETApplication.schedulerProvider.ui())
                    .map { events -> LoadEventsResult.Update(events) }
                    .cast(LoadEventsResult::class.java)
                    .startWith(LoadEventsResult.Loading)
                    .concatWith(Observable.just(LoadEventsResult.Complete))
                    .onErrorReturn(LoadEventsResult::Failure)

            }
        }

    internal var actionProcessor =
        ObservableTransformer<EventsAction, EventsResult> { actions ->
            actions.publish { shared ->
                shared.ofType(LoadEventsAction::class.java)
                    .compose(loadEventsProcessor)
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