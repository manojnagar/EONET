package com.observe.eonet.ui.events

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.observe.eonet.mvibase.MviViewModel
import com.observe.eonet.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class EventsViewModel : ViewModel(), MviViewModel<EventsIntent, EventsViewState> {

    private val intentsSubject: PublishSubject<EventsIntent> = PublishSubject.create()
    private val statesObservable: Observable<EventsViewState> = compose()


    private val _text = MutableLiveData<String>().apply {
        value = "This is events Fragment"
    }
    val text: LiveData<String> = _text

    override fun processIntents(intents: Observable<EventsIntent>) {
        intents.subscribe(intentsSubject)
    }

    private fun compose(): Observable<EventsViewState> {
        val data =
            intentsSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(EventsProcessorHolder().actionProcessor)
                .scan(EventsViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0)

        //TODO: Implement logic here
        return Observable.empty()
    }

    private val intentFilter: ObservableTransformer<EventsIntent, EventsIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.merge(
                    shared.ofType(EventsIntent.LoadEventsIntent::class.java).take(1),
                    shared.notOfType(EventsIntent.LoadEventsIntent::class.java)
                )
            }
        }

    private fun actionFromIntent(intent: EventsIntent): EventsAction {
        Log.e("manoj", "actionFromIntent called  : $intent")
        return when (intent) {
            is EventsIntent.LoadEventsIntent -> EventsAction.LoadEventsAction
            is EventsIntent.TestIntent -> EventsAction.LoadEventsAction
            //TODO: Convert each new intent into action here
        }
    }

    companion object {
        private val reducer =
            BiFunction { previousState: EventsViewState, result: EventsResult ->
                when (result) {
                    is EventsResult.LoadEventsResult -> when (result) {
                        is EventsResult.LoadEventsResult.Loading -> {
                            previousState.copy(isLoading = true)
                        }
                        is EventsResult.LoadEventsResult.Success -> {
                            previousState.copy(isLoading = false)
                        }
                        is EventsResult.LoadEventsResult.Failure -> {
                            previousState
                        }
                    }
                    //TODO: Implement other results
                }
            }
    }


    override fun states(): Observable<EventsViewState> = statesObservable
}