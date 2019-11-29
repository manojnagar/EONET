package com.observe.eonet.ui.eventdetail

import com.observe.eonet.app.EONETApplication
import com.observe.eonet.ui.eventdetail.EventDetailAction.LoadEventDetailAction
import com.observe.eonet.ui.eventdetail.EventDetailResult.LoadEventDetailResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer

class EventDetailProcessorHolder {


    private val loadEventDetailProcessor =
        ObservableTransformer<LoadEventDetailAction, LoadEventDetailResult> { actions ->
            actions.flatMap { action ->
                EONETApplication.dataSource.fetchEvent(action.eventId)
                    .map { event -> LoadEventDetailResult.Success(event) }
                    .cast(LoadEventDetailResult::class.java)
                    .onErrorReturn(LoadEventDetailResult::Failure)
                    .subscribeOn(EONETApplication.schedulerProvider.io())
                    .observeOn(EONETApplication.schedulerProvider.ui())
                    .startWith(LoadEventDetailResult.Loading)
            }
        }

    internal var actionProcessor =
        ObservableTransformer<EventDetailAction, EventDetailResult> { actions ->
            actions.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadEventDetailAction::class.java).compose(
                        loadEventDetailProcessor
                    ),
                    Observable.empty()
                )
                    .cast(EventDetailResult::class.java)
                    .mergeWith(
                        // Error for not implemented actions
                        shared.filter { v ->
                            v !is LoadEventDetailAction
                        }.flatMap { w ->
                            Observable.error<EventDetailResult>(
                                IllegalArgumentException("Unknown Action type: $w")
                            )
                        }
                    )
            }
        }
}