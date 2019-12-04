package com.observe.eonet.mvibase

import androidx.lifecycle.LiveData
import io.reactivex.Observable

interface MviViewModel<I : MviIntent, S : MviViewState> {
  fun processIntents(intents: Observable<I>)
  fun states(): LiveData<S>
}