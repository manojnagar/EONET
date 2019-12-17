package com.observe.eonet.ui.eventdetail

import com.observe.eonet.app.EONETApplication
import com.observe.eonet.ui.eventdetail.EventDetailAction.LoadEventDetailAction
import com.observe.eonet.ui.eventdetail.EventDetailAction.MapReadyAction
import com.observe.eonet.ui.eventdetail.EventDetailResult.LoadEventDetailResult
import com.observe.eonet.ui.eventdetail.EventDetailResult.MapReadyResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer

class EventDetailProcessorHolder {

    private val loadEventDetailProcessor =
        ObservableTransformer<LoadEventDetailAction, LoadEventDetailResult> { actions ->
            actions.flatMap { action ->
                EONETApplication.eventRepository.getEvent(action.eventId)
                    .subscribeOn(EONETApplication.schedulerProvider.io())
                    .observeOn(EONETApplication.schedulerProvider.ui())
                    .doOnNext {
                        println("manoj event info : $it")
                    }
                    .map { event -> LoadEventDetailResult.Success(event) }
                    .cast(LoadEventDetailResult::class.java)
                    .startWith(LoadEventDetailResult.Loading)
                    .concatWith(Observable.just(LoadEventDetailResult.Complete))
                    .onErrorReturn(LoadEventDetailResult::Failure)
            }
        }

    private val mapReadyProcessor =
        ObservableTransformer<MapReadyAction, MapReadyResult> { actions ->
            actions.map {
                MapReadyResult
            }
        }

    internal var actionProcessor =
        ObservableTransformer<EventDetailAction, EventDetailResult> { actions ->
            actions.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadEventDetailAction::class.java).compose(
                        loadEventDetailProcessor
                    ),
                    shared.ofType(MapReadyAction::class.java).compose(
                        mapReadyProcessor
                    )
                )
                    .cast(EventDetailResult::class.java)
                    .mergeWith(
                        // Error for not implemented actions
                        shared.filter { v ->
                            v !is LoadEventDetailAction &&
                                    v !is MapReadyAction
                        }.flatMap { w ->
                            Observable.error<EventDetailResult>(
                                IllegalArgumentException("Unknown Action type: $w")
                            )
                        }
                    )
            }
        }
}