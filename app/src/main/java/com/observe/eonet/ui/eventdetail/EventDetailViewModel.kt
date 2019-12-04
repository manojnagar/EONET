package com.observe.eonet.ui.eventdetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.observe.eonet.mvibase.MviViewModel
import com.observe.eonet.ui.eventdetail.EventDetailAction.LoadEventDetailAction
import com.observe.eonet.ui.eventdetail.EventDetailIntent.LoadEventDetailIntent
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

    private fun compose() {
        disposables.add(intentsSubject
            .doOnNext { Log.d("manoj", "intent received : $it") }
            .compose(intentFilter)
            .doOnNext { Log.d("manoj", "Filtered intent : $it") }
            .map(this::actionFromIntent)
            .doOnNext { Log.d("manoj", "Converted action : $it") }
            .compose(EventDetailProcessorHolder().actionProcessor)
            .doOnNext { Log.d("manoj", "Result : $it") }
            .scan(EventDetailViewState.idle(), reducer)
            .doOnNext { Log.d("manoj", "View state : $it") }
            .distinctUntilChanged()
            .doOnNext { Log.d("manoj", "Different view state : $it") }
            .replay(1)
            .autoConnect(0)
            .subscribe {
                viewStateObservable.postValue(it)
            }
        )
    }

    override fun states(): LiveData<EventDetailViewState> = viewStateObservable

    private fun actionFromIntent(intent: EventDetailIntent): EventDetailAction {
        return when (intent) {
            is LoadEventDetailIntent -> LoadEventDetailAction(intent.eventId)
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
