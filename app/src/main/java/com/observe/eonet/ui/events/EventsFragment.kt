package com.observe.eonet.ui.events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.observe.eonet.R
import com.observe.eonet.mvibase.MviView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class EventsFragment : Fragment(), MviView<EventsIntent, EventsViewState> {

    private val disposables = CompositeDisposable()
    private lateinit var eventsViewModel: EventsViewModel
    private var testIntentPublisher = PublishSubject.create<EventsIntent.TestIntent>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eventsViewModel =
            ViewModelProviders.of(this).get(EventsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_events, container, false)
        val textView: TextView = root.findViewById(R.id.text_events)
        eventsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }

    override fun onStart() {
        super.onStart()
        bind()
    }

    override fun onResume() {
        super.onResume()
        Log.e("manoj", "Resume called")
        testIntentPublisher.onNext(EventsIntent.TestIntent)
    }

    private fun bind() {
        disposables.add(eventsViewModel.states().subscribe(this::render))
        eventsViewModel.processIntents(intents())
    }

    private fun loadIntent(): Observable<EventsIntent.LoadEventsIntent> {
        return Observable.just(EventsIntent.LoadEventsIntent)
    }

    //TODO: Create same logic for each user intent
    private fun testIntent(): Observable<EventsIntent.TestIntent> {
        return testIntentPublisher
    }

    override fun intents(): Observable<EventsIntent> {
        return Observable.merge(
            loadIntent(),
            testIntent()
        )
    }

    override fun render(state: EventsViewState) {
        Log.e("manoj", "Render called : $state")
        //TODO: Implement logic here
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}