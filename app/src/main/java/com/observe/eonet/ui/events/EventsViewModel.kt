package com.observe.eonet.ui.events

import android.util.Log
import androidx.lifecycle.ViewModel
import com.observe.eonet.mvibase.MviViewModel
import com.observe.eonet.ui.events.EventsAction.*
import com.observe.eonet.ui.events.EventsIntent.*
import com.observe.eonet.ui.events.EventsResult.LoadEventsResult
import com.observe.eonet.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class EventsViewModel : ViewModel(), MviViewModel<EventsIntent, EventsViewState> {

    private val intentsSubject: PublishSubject<EventsIntent> = PublishSubject.create()
    private val statesObservable: Observable<EventsViewState> = compose()

    override fun processIntents(intents: Observable<EventsIntent>) {
        intents.subscribe(intentsSubject)
    }

    private fun compose(): Observable<EventsViewState> {
        return intentsSubject
            .doOnNext {
                Log.d(TAG, "New intent : $it")
            }
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .doOnNext {

                Log.d(TAG, "Converted action : $it")
            }
            .compose(EventsProcessorHolder().actionProcessor)
            .doOnNext {
                Log.d(TAG, "Converted result : $it")
            }
            .scan(EventsViewState.idle(), reducer)
            .doOnNext {
                Log.d(TAG, "Converted viewState : $it")
            }
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
            .doOnNext {
                Log.d(TAG, "Notify view state to UI : $it")
            }
    }

    private val intentFilter: ObservableTransformer<EventsIntent, EventsIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadEventsIntent::class.java).take(1),
                    shared.notOfType(LoadEventsIntent::class.java)
                )
            }
        }

    private fun actionFromIntent(intent: EventsIntent): EventsAction {
        return when (intent) {
            is LoadEventsIntent -> LoadEventsAction
            is SelectEventIntent -> SelectEventAction(intent.event)
            is DetailPageOpenedIntent -> DetailPageOpenedAction

            //TODO: Convert each new intent into action here
        }
    }

    companion object {
        private const val TAG = "EventsVM"

        private val reducer =
            BiFunction { previousState: EventsViewState, result: EventsResult ->
                when (result) {
                    is LoadEventsResult -> when (result) {
                        is LoadEventsResult.Loading -> {
                            previousState.copy(isLoading = true)
                        }
                        is LoadEventsResult.Success -> {
                            val newList = previousState.events.toMutableList()
                            newList.addAll(result.events)
                            previousState.copy(isLoading = false, events = newList)
                        }
                        is LoadEventsResult.Failure -> {
                            previousState.copy(isLoading = false, error = result.error)
                        }
                    }

                    is EventsResult.SelectEventResult -> previousState.copy(
                        isEventSelected = true,
                        selectedEvent = result.event
                    )

                    is EventsResult.DetailPageOpenedResult ->
                        previousState.copy(
                            isEventSelected = false,
                            selectedEvent = null
                        )
                    //TODO: Implement other results
                }
            }
    }


    override fun states(): Observable<EventsViewState> = statesObservable
}