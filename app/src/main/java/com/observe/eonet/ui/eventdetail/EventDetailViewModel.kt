package com.observe.eonet.ui.eventdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.observe.eonet.mvibase.MviViewModel
import com.observe.eonet.ui.eventdetail.EventDetailAction.LoadEventDetailAction
import com.observe.eonet.ui.eventdetail.EventDetailIntent.*
import com.observe.eonet.ui.eventdetail.EventDetailResult.LoadEventDetailResult
import com.observe.eonet.ui.eventdetail.EventDetailResult.MapReadyResult
import com.observe.eonet.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class EventDetailViewModel : ViewModel(),
    MviViewModel<EventDetailIntent, EventDetailViewState> {
    
    private val disposables = CompositeDisposable()
    private val viewStateObservable: MutableLiveData<EventDetailViewState> = MutableLiveData()
    private val intentsSubject: PublishSubject<EventDetailIntent> = PublishSubject.create()

    init {
        compose()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

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


    private fun mapReadyIntentProcessor(intent: MapReadyIntent): EventDetailViewState {
        return viewStateObservable.value ?: EventDetailViewState.idle()
    }

    private fun compose() {
        val subscription = intentsSubject
            .publish { shared ->
                Observable.merge(
                    shared.ofType(MapReadyIntent::class.java)
                        .map(this::mapReadyIntentProcessor),
                    Observable.merge(
                        shared.ofType(LoadEventDetailIntent::class.java),
                        shared.ofType(RetryLoadEventIntent::class.java)
                    )
                        .compose(intentFilter)
                        .map(this::actionFromIntent)
                        .compose(EventDetailProcessorHolder().actionProcessor)
                        .scan(EventDetailViewState.idle(), reducer)
                )
            }
            .replay(1)
            .autoConnect(0)
            .subscribe {
                viewStateObservable.postValue(it)
            }
        disposables.add(subscription)
    }

    override fun states(): LiveData<EventDetailViewState> = viewStateObservable

    private fun actionFromIntent(intent: EventDetailIntent): EventDetailAction {
        return when (intent) {
            is LoadEventDetailIntent -> LoadEventDetailAction(intent.eventId)
            is MapReadyIntent -> EventDetailAction.MapReadyAction
            is RetryLoadEventIntent -> LoadEventDetailAction(intent.eventId)
        }
    }

    companion object {
        private val reducer =
            BiFunction { previousState: EventDetailViewState, result: EventDetailResult ->
                when (result) {
                    //Handle load event case
                    is LoadEventDetailResult ->
                        when (result) {
                            is LoadEventDetailResult.Loading ->
                                EventDetailViewState.LoadingView
                            is LoadEventDetailResult.Success ->
                                EventDetailViewState.DataView(result.event)
                            is LoadEventDetailResult.Failure ->
                                EventDetailViewState.ErrorView(result.error.localizedMessage ?: "")
                            is LoadEventDetailResult.Complete ->
                                if (previousState is EventDetailViewState.DataView) {
                                    previousState
                                } else {
                                    EventDetailViewState.EventNotExistView
                                }
                        }

                    is MapReadyResult ->
                        previousState
                }
            }
    }
}
