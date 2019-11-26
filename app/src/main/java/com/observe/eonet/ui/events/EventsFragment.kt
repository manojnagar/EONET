package com.observe.eonet.ui.events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.observe.eonet.R
import com.observe.eonet.mvibase.MviView
import com.observe.eonet.util.visible
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_events.*

class EventsFragment : Fragment(), MviView<EventsIntent, EventsViewState> {

    private val disposables = CompositeDisposable()
    private val adapter = EventsAdapter(mutableListOf())
    private lateinit var eventsViewModel: EventsViewModel
    private var testIntentPublisher = PublishSubject.create<EventsIntent.TestIntent>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eventsViewModel =
            ViewModelProviders.of(this).get(EventsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup views
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        bind()
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
        Log.d(TAG, "Rendering viewState on UI -> $state")
        progressBar.visible = state.isLoading

        if (state.events.isEmpty()) {
            emptyState.visible = true
            eventsRecyclerView.visible = false
        } else {
            emptyState.visible = false
            eventsRecyclerView.visible = true
            adapter.appendEvents(state.events)
        }

        if (state.error != null) {
            Toast.makeText(context, getString(R.string.error_loading_events), Toast.LENGTH_SHORT)
                .show()
            Log.e(TAG, "Error loading events: ${state.error.localizedMessage}")
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    companion object {
        private const val TAG = "EventsFragment"
    }
}