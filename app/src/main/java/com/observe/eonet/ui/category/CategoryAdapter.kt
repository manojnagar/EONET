package com.observe.eonet.ui.category

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.observe.eonet.R
import com.observe.eonet.app.inflate
import com.observe.eonet.data.model.EOCategory
import kotlinx.android.synthetic.main.list_item_event.view.*

class CategoryAdapter(
    private val categories: MutableList<EOCategory>,
    private val callback: AdapterCallback
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.list_item_category))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], callback)
    }

    override fun getItemCount(): Int = categories.size

    fun appendCategories(categories: List<EOCategory>) {
        this.categories.clear()
        this.categories.addAll(categories)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var category: EOCategory

        fun bind(category: EOCategory, callback: AdapterCallback) {
            this.category = category

            //Update view
            itemView.title.text = category.title

            itemView.setOnClickListener {
                callback.onCategorySelected(category)
            }
        }
    }

    interface AdapterCallback {
        fun onCategorySelected(category: EOCategory)
    }
}