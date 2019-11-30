package com.observe.eonet.ui.category

import androidx.lifecycle.ViewModel
import com.observe.eonet.mvibase.MviViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CategoryViewModel : ViewModel(), MviViewModel<CategoriesIntent, CategoriesViewState> {

    private val intentsSubject: PublishSubject<CategoriesIntent> = PublishSubject.create()

    override fun processIntents(intents: Observable<CategoriesIntent>) {
        intents.subscribe(intentsSubject)
    }

    override fun states(): Observable<CategoriesViewState> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}