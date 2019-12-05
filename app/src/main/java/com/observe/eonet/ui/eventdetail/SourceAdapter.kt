package com.observe.eonet.ui.eventdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.observe.eonet.R
import com.observe.eonet.data.model.EOSource
import kotlinx.android.synthetic.main.list_item_event.view.title
import kotlinx.android.synthetic.main.list_item_source.view.*

class SourceAdapter(
    private val sourceList: MutableList<EOSource>,
    private val callback: AdapterCallback
) :
    RecyclerView.Adapter<SourceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_source,
                parent, false
            )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = sourceList.size


    fun updateSourceList(newSourceList: List<EOSource>) {
        sourceList.clear()
        sourceList.addAll(newSourceList)
        notifyDataSetChanged()
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sourceList[position], callback)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(source: EOSource, callback: AdapterCallback) {
            view.title.text = source.id
            view.url.text = source.url
            view.setOnClickListener {
                callback.onSourceSelected(source)
            }
        }
    }

    interface AdapterCallback {
        fun onSourceSelected(source: EOSource)
    }
}