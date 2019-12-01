package com.observe.eonet.ui.eventdetail

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.observe.eonet.R
import com.observe.eonet.data.model.EOSource
import com.observe.eonet.mvibase.MviView
import com.observe.eonet.util.RecyclerViewItemDecoration
import com.observe.eonet.util.visible
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.event_detail_fragment.*
import kotlinx.android.synthetic.main.fragment_events.emptyState
import kotlinx.android.synthetic.main.fragment_events.progressBar

class EventDetailFragment : Fragment(),
    MviView<EventDetailIntent, EventDetailViewState>,
    SourceAdapter.AdapterCallback {

    private val disposable = CompositeDisposable()
    private val args: EventDetailFragmentArgs by navArgs()
    private lateinit var viewModel: EventDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this, EventDetailViewModelFactory(args.eventId))
            .get(EventDetailViewModel::class.java)
        return inflater.inflate(R.layout.event_detail_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    private fun bindViewModel() {
        disposable.add(viewModel.states().subscribe(this::render))
        viewModel.processIntents(intents())
    }

    override fun intents(): Observable<EventDetailIntent> {
        return Observable.just(EventDetailIntent.LoadEventDetailIntent)
    }

    override fun render(state: EventDetailViewState) {
        progressBar.visible = state.isLoading

        if (state.event == null) {
            emptyState.visible = !state.isLoading
            title.visible = false
            category.visible = false
            sourceTitle.visible = false
        } else {
            emptyState.visible = false
            title.visible = true
            title.text = state.event.title

            category.visible = true
            var categoryData = state.event.categories.joinToString(" #") { it.title }
            categoryData = "#$categoryData"
            category.text = categoryData

            //Setup source recycler view
            sourceTitle.visible = true
            sourceRecyclerView.layoutManager = LinearLayoutManager(context)
            sourceRecyclerView.adapter = SourceAdapter(state.event.sources, this)
            context?.let {
                sourceRecyclerView
                    .addItemDecoration(
                        RecyclerViewItemDecoration(
                            it.resources.getDimensionPixelSize(R.dimen.events_card_item_layout_margin),
                            ContextCompat.getColor(it, R.color.event_divider_color),
                            it.resources.getDimensionPixelSize(R.dimen.events_card_item_divider_height)
                        )
                    )
            }
        }

        if (state.error != null) {
            Toast.makeText(context, getString(R.string.error_loading_events), Toast.LENGTH_SHORT)
                .show()
            Log.e(TAG, "Error loading event detail: ${state.error.localizedMessage}")
        }
    }

    companion object {
        private const val TAG = "EventDetailFragment"
    }

    override fun onSourceSelected(source: EOSource) {
        val direction =
            EventDetailFragmentDirections.actionEventDetailFragmentToWebContentFragment(source.url)
        findNavController().navigate(direction)
    }
}
