package com.observe.eonet.ui.events

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.observe.eonet.R
import com.observe.eonet.app.inflate
import com.observe.eonet.data.model.EOEvent
import com.observe.eonet.util.convertToString
import kotlinx.android.synthetic.main.list_item_event.view.*

class EventsAdapter(
    private val events: MutableList<EOEvent>,
    private val callback: AdapterCallback
) :
    RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.list_item_event))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position], callback)
    }

    override fun getItemCount(): Int = events.size

    fun appendEvents(events: List<EOEvent>) {
        this.events.clear()
        this.events.addAll(events)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var event: EOEvent

        fun bind(event: EOEvent, callback: AdapterCallback) {
            this.event = event

            //Update view
            var category = event.categories.joinToString(" #") { it.title }
            category = "#$category"
            event.startDate?.convertToString()?.let {
                category = category.plus(", Date: $it")
            }
            itemView.category.text = category
            itemView.title.text = event.title

            itemView.setOnClickListener {
                callback.onEventSelected(event)
            }
        }
    }

    interface AdapterCallback {
        fun onEventSelected(event: EOEvent)
    }
}