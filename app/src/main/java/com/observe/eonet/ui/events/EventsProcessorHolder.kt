package com.observe.eonet.ui.events

import io.reactivex.Observable
import io.reactivex.ObservableTransformer

class EventsProcessorHolder {

    private val loadEventsProcessor =
        ObservableTransformer<EventsAction, EventsResult> { actions ->
            actions.flatMap { action ->
                //TODO: Add business logic here to convert an action to result
                Observable.just(resultFromAction(action))
            }
        }

    //TODO: Dummy implementation to convert an action into result
    private fun resultFromAction(action: EventsAction): EventsResult {
        return when (action) {
            is EventsAction.LoadEventsAction -> EventsResult.LoadEventsResult.Loading
        }
    }

    internal var actionProcessor =
        ObservableTransformer<EventsAction, EventsResult> { actions ->
            actions.publish { shared ->
                Observable.merge(
                    shared.ofType(EventsAction.LoadEventsAction::class.java)
                        .compose(loadEventsProcessor),
                    Observable.empty()
                ).mergeWith(
                    // Error for not implemented actions
                    shared.filter { v ->
                        v !is EventsAction.LoadEventsAction
                    }.flatMap { w ->
                        Observable.error<EventsResult>(
                            IllegalArgumentException("Unknown Action type: $w")
                        )
                    }
                )
            }
        }
}