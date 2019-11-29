package com.observe.eonet.ui.eventdetail

import androidx.lifecycle.ViewModel
import com.observe.eonet.mvibase.MviViewModel
import com.observe.eonet.ui.eventdetail.EventDetailAction.LoadEventDetailAction
import com.observe.eonet.ui.eventdetail.EventDetailIntent.LoadEventDetailIntent
import com.observe.eonet.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class EventDetailViewModel(private val eventId: String) : ViewModel(),
    MviViewModel<EventDetailIntent, EventDetailViewState> {

    private val intentsSubject: PublishSubject<EventDetailIntent> = PublishSubject.create()

    override fun processIntents(intents: Observable<EventDetailIntent>) {
        intents.subscribe(intentsSubject)
    }

    private val intentFilter: ObservableTransformer<EventDetailIntent, EventDetailIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadEventDetailIntent::class.java).take(1),
                    shared.notOfType(LoadEventDetailIntent::class.java)
                )
            }
        }


    override fun states(): Observable<EventDetailViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(EventDetailProcessorHolder().actionProcessor)
            .scan(EventDetailViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)


    }

    private fun actionFromIntent(intent: EventDetailIntent): EventDetailAction {
        return when (intent) {
            is LoadEventDetailIntent -> LoadEventDetailAction(eventId)
        }
    }

    companion object {
        private val reducer =
            BiFunction { previousState: EventDetailViewState, result: EventDetailResult ->
                when (result) {

                    //Handle load event case
                    is EventDetailResult.LoadEventDetailResult ->
                        when (result) {
                            is EventDetailResult.LoadEventDetailResult.Loading ->
                                previousState.copy(isLoading = true)
                            is EventDetailResult.LoadEventDetailResult.Success ->
                                previousState.copy(isLoading = false, event = result.event)
                            is EventDetailResult.LoadEventDetailResult.Failure ->
                                previousState.copy(
                                    isLoading = false,
                                    event = null,
                                    error = result.error
                                )
                        }
                }
            }
    }
}
