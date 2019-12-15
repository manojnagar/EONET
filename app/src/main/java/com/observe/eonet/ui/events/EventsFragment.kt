package com.observe.eonet.ui.events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.observe.eonet.R
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.mvibase.MviView
import com.observe.eonet.ui.events.EventsIntent.LoadEventsIntent
import com.observe.eonet.util.RecyclerViewItemDecoration
import com.observe.eonet.util.makeInVisible
import com.observe.eonet.util.makeVisible
import com.observe.eonet.util.visible
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.fragment_events.view.*

class EventsFragment : Fragment(), MviView<EventsIntent, EventsViewState>,
    EventsAdapter.AdapterCallback {

    private val args: EventsFragmentArgs by navArgs()
    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var adapter: EventsAdapter
    private var pullToRefreshIntentPublisher =
        PublishSubject.create<EventsIntent.PullToRefreshIntent>()
    private var retryLoadIntentPublisher =
        PublishSubject.create<EventsIntent.RetryLoadEventIntent>()

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

        retryButton.setOnClickListener {
            retryLoadIntentPublisher.onNext(EventsIntent.RetryLoadEventIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        bind()
    }

    private fun bind() {
        eventsViewModel.states().observe(this, Observer<EventsViewState> {
            render(it)
        })
        eventsViewModel.processIntents(intents())
    }

    private fun loadIntent(): Observable<LoadEventsIntent> {
        return Observable.just(LoadEventsIntent(args.categoryId))
    }

    //TODO: Create same logic for each user intent
    private fun pullToRefreshIntent(): Observable<EventsIntent.PullToRefreshIntent> {
        return pullToRefreshIntentPublisher
    }

    private fun retryLoadEventIntent(): Observable<EventsIntent.RetryLoadEventIntent> {
        return retryLoadIntentPublisher
    }

    override fun intents(): Observable<EventsIntent> {
        return Observable.merge(
            loadIntent(),
            pullToRefreshIntent(),
            retryLoadEventIntent()
        )
    }

    override fun render(state: EventsViewState) {
        Log.d(TAG, "Rendering viewState on UI -> $state")

        when (state) {
            is EventsViewState.LoadingView -> {
                makeInVisible(errorView, emptyView, dataView)
                loadingView.makeVisible()
            }
            is EventsViewState.ErrorView -> {
                makeInVisible(loadingView, emptyView, dataView)
                errorView.makeVisible()
                errorView.errorMessageView.text = state.message
            }
            is EventsViewState.EmptyView -> {
                makeInVisible(loadingView, errorView, dataView)
                emptyView.makeVisible()
            }
            is EventsViewState.DataView -> {
                makeInVisible(loadingView, emptyView, errorView)
                dataView.makeVisible()

                loadingResultProgressBar.visible = state.isLoadingInProgress
                adapter.appendEvents(state.events)

                state.toastMessage?.let {
                    Toast.makeText(
                        context,
                        getString(R.string.error_loading_events),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error loading events: $it")
                }
            }
        }
    }

    companion object {
        private const val TAG = "EventsFragment"
    }

    override fun onEventSelected(event: EOEvent) {
        val direction =
            EventsFragmentDirections.actionNavigationEventsToEventDetailFragment(event.id)
        findNavController().navigate(direction)
    }
}