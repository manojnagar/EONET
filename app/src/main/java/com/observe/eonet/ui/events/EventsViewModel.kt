package com.observe.eonet.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviViewModel
import com.observe.eonet.ui.events.EventsAction.*
import com.observe.eonet.ui.events.EventsIntent.LoadEventsIntent
import com.observe.eonet.ui.events.EventsResult.LoadEventsResult
import com.observe.eonet.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class EventsViewModel : ViewModel(), MviViewModel<EventsIntent, EventsViewState> {

    private val disposables = CompositeDisposable()
    private val intentsSubject: PublishSubject<EventsIntent> = PublishSubject.create()
    private val viewStateObservable: MutableLiveData<EventsViewState> = MutableLiveData()

    init {
        compose()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    override fun processIntents(intents: Observable<EventsIntent>) {
        intents.subscribe(intentsSubject)
    }

    private fun compose() {
        disposables.add(intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(EventsProcessorHolder().actionProcessor)
            .scan(EventsViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
            .subscribe {
                viewStateObservable.postValue(it)
            }
        )
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
            is LoadEventsIntent -> LoadEventsAction(intent.categoryId)
            is EventsIntent.PullToRefreshIntent -> LoadEventsAction(null)
            is EventsIntent.RetryLoadEventIntent -> LoadEventsAction(null)
            is EventsIntent.UserQueryChangeIntent -> FilterEventsAction(intent.newText)
        }
    }

    companion object {
        private const val TAG = "EventsVM"

        private val reducer =
            BiFunction { previousState: EventsViewState, result: EventsResult ->
                when (result) {
                    is LoadEventsResult -> when (result) {
                        is LoadEventsResult.Loading -> {
                            EventsViewState.LoadingView
                        }

                        is LoadEventsResult.Update -> {
                            val events = mutableListOf<EOEvent>()
                            if (previousState is EventsViewState.DataView) {
                                events.addAll(previousState.events)
                            }
                            events.addAll(result.events)

                            //Return result state
                            if (previousState is EventsViewState.DataView) {
                                previousState.copy(events = events)
                            } else {
                                EventsViewState.DataView(
                                    isLoadingInProgress = true,
                                    events = events,
                                    toastMessage = null
                                )
                            }
                        }

                        is LoadEventsResult.Complete -> {
                            if (previousState is EventsViewState.DataView) {
                                if (previousState.events.isEmpty()) {
                                    EventsViewState.EmptyView
                                } else {
                                    previousState.copy(isLoadingInProgress = false)
                                }
                            } else {
                                EventsViewState.EmptyView
                            }
                        }

                        is LoadEventsResult.Failure -> {
                            EventsViewState.ErrorView(result.error.localizedMessage ?: "")
                        }
                    }
                    //TODO: Implement other results
                }
            }
    }


    override fun states(): LiveData<EventsViewState> = viewStateObservable
}