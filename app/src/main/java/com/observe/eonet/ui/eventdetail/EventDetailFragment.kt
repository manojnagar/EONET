package com.observe.eonet.ui.eventdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.observe.eonet.R

class EventDetailFragment : Fragment() {

    companion object {
        fun newInstance() = EventDetailFragment()
    }

    private lateinit var viewModel: EventDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(EventDetailViewModel::class.java)
        return inflater.inflate(R.layout.event_detail_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
    }

    private fun bindViewModel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
