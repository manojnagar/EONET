package com.observe.eonet.ui.events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.observe.eonet.R
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviView
import com.observe.eonet.ui.events.EventsIntent.*
import com.observe.eonet.util.RecyclerViewItemDecoration
import com.observe.eonet.util.visible
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_events.*

class EventsFragment : Fragment(), MviView<EventsIntent, EventsViewState>,
    EventsAdapter.AdapterCallback {

    private val disposables = CompositeDisposable()
    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var adapter: EventsAdapter
    private var selectEventIntentPublisher =
        PublishSubject.create<SelectEventIntent>()
    private var detailPageOpenedIntentPublisher =
        PublishSubject.create<DetailPageOpenedIntent>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eventsViewModel =
            ViewModelProviders.of(this).get(EventsViewModel::class.java)
        adapter = EventsAdapter(mutableListOf(), this)
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup views
        eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        eventsRecyclerView.adapter = adapter

        context?.let {
            eventsRecyclerView
                .addItemDecoration(
                    RecyclerViewItemDecoration(
                        it.resources.getDimensionPixelSize(R.dimen.events_card_item_layout_margin),
                        ContextCompat.getColor(it, R.color.event_divider_color),
                        it.resources.getDimensionPixelSize(R.dimen.events_card_item_divider_height)
                    )
                )
        }
    }

    override fun onStart() {
        super.onStart()
        bind()
    }

    private fun bind() {
        disposables.add(eventsViewModel.states().subscribe(this::render))
        eventsViewModel.processIntents(intents())
    }

    private fun loadIntent(): Observable<LoadEventsIntent> {
        return Observable.just(LoadEventsIntent)
    }

    //TODO: Create same logic for each user intent
    private fun selectEventIntent(): Observable<SelectEventIntent> {
        return selectEventIntentPublisher
    }

    private fun detailPageOpenedIntent(): Observable<DetailPageOpenedIntent> {
        return detailPageOpenedIntentPublisher
    }

    override fun intents(): Observable<EventsIntent> {
        return Observable.merge(
            loadIntent(),
            selectEventIntent(),
            detailPageOpenedIntent()
        )
    }

    override fun render(state: EventsViewState) {
        Log.d(TAG, "Rendering viewState on UI -> $state")
        if (state.isEventSelected) {
            findNavController().navigate(R.id.action_navigation_events_to_eventDetailFragment)
            detailPageOpenedIntentPublisher.onNext(DetailPageOpenedIntent)
            return
        }

        progressBar.visible = state.isLoading

        if (state.events.isEmpty()) {
            emptyState.visible = !state.isLoading
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

    override fun onEventSelected(event: EOEvent) {
        Log.e("manoj", "onEvent Selected : $event")
//        selectEventIntentPublisher.onNext(SelectEventIntent(event))
        findNavController().navigate(R.id.action_navigation_events_to_eventDetailFragment)
    }
}