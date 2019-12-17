package com.observe.eonet.ui.category

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.observe.eonet.R
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.firebase.AnalyticsManager
import com.observe.eonet.mvibase.MviView
import com.observe.eonet.ui.category.CategoriesIntent.LoadCategoriesIntent
import com.observe.eonet.ui.category.CategoriesIntent.RetryLoadCategoriesIntent
import com.observe.eonet.util.RecyclerViewItemDecoration
import com.observe.eonet.util.makeInVisible
import com.observe.eonet.util.makeVisible
import com.observe.eonet.util.visible
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.loading_layout.*

class CategoryFragment : Fragment(), CategoryAdapter.AdapterCallback,
    MviView<CategoriesIntent, CategoriesViewState> {

    private val disposables = CompositeDisposable()
    private lateinit var adapter: CategoryAdapter
    private lateinit var categoryViewModel: CategoryViewModel
    private var retryLoadIntentPublisher =
        PublishSubject.create<RetryLoadCategoriesIntent>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
        AnalyticsManager.reportScreenViewEvent("categories")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        categoryViewModel =
            ViewModelProviders.of(this).get(CategoryViewModel::class.java)
        adapter = CategoryAdapter(mutableListOf(), this)
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryRecyclerView.adapter = adapter

        context?.let {
            categoryRecyclerView
                .addItemDecoration(
                    RecyclerViewItemDecoration(
                        it.resources.getDimensionPixelSize(R.dimen.events_card_item_layout_margin),
                        ContextCompat.getColor(it, R.color.event_divider_color),
                        it.resources.getDimensionPixelSize(R.dimen.events_card_item_divider_height)
                    )
                )
        }

        retryButton.setOnClickListener {
            retryLoadIntentPublisher.onNext(RetryLoadCategoriesIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
    }

    private fun bindViewModel() {
        categoryViewModel.states().observe(this, Observer<CategoriesViewState> {
            render(it)
        })
        categoryViewModel.processIntents(intents())
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_by -> {
                val direction =
                    CategoryFragmentDirections.actionNavigationCategoryToFilterFragment()
                findNavController().navigate(direction)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCategorySelected(category: EOCategory) {
        val direction =
            CategoryFragmentDirections.actionNavigationCategoryToNavigationEvents(category.id)
        findNavController().navigate(direction)
    }

    private fun retryLoadCategories(): Observable<RetryLoadCategoriesIntent> {
        return retryLoadIntentPublisher
    }

    override fun intents(): Observable<CategoriesIntent> {
        return Observable.merge(
            Observable.just(LoadCategoriesIntent),
            retryLoadCategories()
        )
    }

    override fun render(state: CategoriesViewState) {
        Log.d(TAG, "New categories screen UI state : $state")
        when (state) {
            is CategoriesViewState.LoadingView -> {
                makeInVisible(errorView, emptyView, dataView)
                loadingView.makeVisible()
            }
            is CategoriesViewState.EmptyView -> {
                makeInVisible(loadingView, errorView, dataView)
                emptyView.makeVisible()
                emptyViewTitle.setText(R.string.no_category_found)
            }
            is CategoriesViewState.ErrorView -> {
                makeInVisible(loadingView, emptyView, dataView)
                errorView.makeVisible()
            }
            is CategoriesViewState.DataView -> {
                makeInVisible(loadingView, emptyView, errorView)
                dataView.makeVisible()

                //Bind data
                loadingResultProgressBar.visible = state.isLoadingInProgress
                adapter.appendCategories(state.categories)

                state.toastMessage?.let {
                    Toast.makeText(
                        context,
                        getString(R.string.error_loading_events),
                        Toast.LENGTH_SHORT
                    )
                .show()
                    Log.e(TAG, "Error loading categories: $it")
                }
            }
        }
    }

    companion object {
        private const val TAG = "CategoriesFragment"
    }
}