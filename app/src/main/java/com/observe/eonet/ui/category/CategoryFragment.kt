package com.observe.eonet.ui.category

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
import com.observe.eonet.data.model.EOCategory
import com.observe.eonet.mvibase.MviView
import com.observe.eonet.ui.category.CategoriesIntent.LoadCategoriesIntent
import com.observe.eonet.util.RecyclerViewItemDecoration
import com.observe.eonet.util.visible
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_category.*

class CategoryFragment : Fragment(), CategoryAdapter.AdapterCallback,
    MviView<CategoriesIntent, CategoriesViewState> {

    private val disposables = CompositeDisposable()
    private lateinit var adapter: CategoryAdapter
    private lateinit var categoryViewModel: CategoryViewModel

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
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
    }

    private fun bindViewModel() {
        disposables.add(categoryViewModel.states().subscribe(this::render))
        categoryViewModel.processIntents(intents())
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onCategorySelected(category: EOCategory) {
        val direction =
            CategoryFragmentDirections.actionNavigationCategoryToNavigationEvents(category.id)
        findNavController().navigate(direction)
    }

    override fun intents(): Observable<CategoriesIntent> {
        return Observable.just(LoadCategoriesIntent)
    }

    override fun render(state: CategoriesViewState) {
        progressBar.visible = state.isLoading

        updatingResultProgressBar.visible = !state.isUpdateComplete

        if (state.categories.isEmpty()) {
            emptyState.visible = !state.isLoading
            categoryRecyclerView.visible = false
        } else {
            emptyState.visible = false
            categoryRecyclerView.visible = true
            adapter.appendCategories(state.categories)
        }

        if (state.error != null) {
            Toast.makeText(context, getString(R.string.error_loading_events), Toast.LENGTH_SHORT)
                .show()
            Log.e(TAG, "Error loading categories: ${state.error.localizedMessage}")
        }
    }

    companion object {
        private const val TAG = "CategoriesFragment"
    }
}