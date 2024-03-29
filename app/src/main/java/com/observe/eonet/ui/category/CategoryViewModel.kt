package com.observe.eonet.ui.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.observe.eonet.mvibase.MviViewModel
import com.observe.eonet.ui.category.CategoriesAction.LoadCategoriesAction
import com.observe.eonet.ui.category.CategoriesIntent.LoadCategoriesIntent
import com.observe.eonet.ui.category.CategoriesIntent.RetryLoadCategoriesIntent
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
            is RetryLoadCategoriesIntent -> LoadCategoriesAction
        }
    }

    override fun states(): LiveData<CategoriesViewState> = viewStateObservable

    companion object {
        private val TAG = "CategoryVM"
        private val reducer =
            BiFunction { previousState: CategoriesViewState, result: CategoriesResult ->
                Log.d(TAG, "Category VM new result: $result")
                when (result) {
                    is LoadCategoriesResult -> when (result) {
                        is LoadCategoriesResult.Loading -> CategoriesViewState.LoadingView

                        is LoadCategoriesResult.Update -> {
                            if (previousState is CategoriesViewState.DataView) {
                                previousState.copy(categories = result.categories)
                            } else {
                                CategoriesViewState.DataView(
                                    isLoadingInProgress = true,
                                    categories = result.categories,
                                    toastMessage = null
                                )
                            }
                        }

                        is LoadCategoriesResult.Complete -> {
                            if (previousState is CategoriesViewState.DataView) {
                                if (previousState.categories.isEmpty()) {
                                    CategoriesViewState.EmptyView
                                } else {
                                    previousState.copy(isLoadingInProgress = false)
                                }
                            } else {
                                CategoriesViewState.EmptyView
                            }
                        }

                        is LoadCategoriesResult.Failure -> {
                            CategoriesViewState.ErrorView(
                                result.error.localizedMessage ?: "Something went wrong"
                            )
                        }

                    }
                }
            }
    }
}