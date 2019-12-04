package com.observe.eonet.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.observe.eonet.mvibase.MviViewModel
import com.observe.eonet.ui.category.CategoriesAction.LoadCategoriesAction
import com.observe.eonet.ui.category.CategoriesIntent.LoadCategoriesIntent
import com.observe.eonet.ui.category.CategoriesResult.LoadCategoriesResult
import com.observe.eonet.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class CategoryViewModel : ViewModel(), MviViewModel<CategoriesIntent, CategoriesViewState> {


    private val disposables = CompositeDisposable()
    private val viewStateObservable: MutableLiveData<CategoriesViewState> = MutableLiveData()
    private val intentsSubject: PublishSubject<CategoriesIntent> = PublishSubject.create()

    init {
        compose()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun compose() {
        disposables.add(intentsSubject
            .compose(intentsFilter)
            .map(this::actionFromIntent)
            .compose(CategoriesProcessorHolder().actionProcessor)
            .scan(CategoriesViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
            .subscribe {
                viewStateObservable.postValue(it)
            }
        )
    }

    override fun processIntents(intents: Observable<CategoriesIntent>) {
        intents.subscribe(intentsSubject)
    }

    private val intentsFilter: ObservableTransformer<CategoriesIntent, CategoriesIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadCategoriesIntent::class.java).take(1),
                    shared.notOfType(LoadCategoriesIntent::class.java)
                )
            }
        }

    private fun actionFromIntent(intent: CategoriesIntent): CategoriesAction {
        return when (intent) {
            is LoadCategoriesIntent -> LoadCategoriesAction
        }
    }

    override fun states(): LiveData<CategoriesViewState> = viewStateObservable

    companion object {
        private val reducer =
            BiFunction { previousState: CategoriesViewState, result: CategoriesResult ->
                when (result) {
                    is LoadCategoriesResult -> when (result) {
                        is LoadCategoriesResult.Loading -> previousState.copy(
                            isLoading = true,
                            isUpdateComplete = true
                        )

                        is LoadCategoriesResult.Update -> {
                            previousState.copy(
                                isLoading = false,
                                isUpdateComplete = false,
                                categories = result.categories
                            )
                        }

                        is LoadCategoriesResult.Complete -> {
                            previousState.copy(isLoading = false, isUpdateComplete = true)
                        }

                        is LoadCategoriesResult.Failure ->
                            previousState.copy(
                                isLoading = false, isUpdateComplete = true,
                                error = result.error
                            )
                    }
                }
            }
    }
}