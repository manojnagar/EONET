package com.observe.eonet.ui.eventdetail

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.observe.eonet.R
import com.observe.eonet.data.model.EOBaseGeometry
import com.observe.eonet.data.model.EOBaseGeometry.EOPointGeometry
import com.observe.eonet.data.model.EOBaseGeometry.EOPolygonGeometry
import com.observe.eonet.data.model.EOSource
import com.observe.eonet.firebase.AnalyticsManager
import com.observe.eonet.mvibase.MviView
import com.observe.eonet.ui.eventdetail.EventDetailIntent.*
import com.observe.eonet.util.RecyclerViewItemDecoration
import com.observe.eonet.util.formattedDateTime
import com.observe.eonet.util.makeInVisible
import com.observe.eonet.util.makeVisible
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.event_detail_fragment.*
import kotlinx.android.synthetic.main.loading_layout.*

class EventDetailFragment : Fragment(),
    MviView<EventDetailIntent, EventDetailViewState>,
    SourceAdapter.AdapterCallback, OnMapReadyCallback {

    companion object {
        private const val TAG = "EventDetailFragment"
    }

    private val args: EventDetailFragmentArgs by navArgs()
    private lateinit var viewModel: EventDetailViewModel
    private var readyMap: GoogleMap? = null
    private lateinit var adapter: SourceAdapter

    private val mapReadyIntentPublisher =
        PublishSubject.create<MapReadyIntent>()
    private val retryLoadEventIntentPublisher =
        PublishSubject.create<RetryLoadEventIntent>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
        AnalyticsManager.reportScreenViewEvent("event")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(EventDetailViewModel::class.java)
        return inflater.inflate(R.layout.event_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        googleMapView.getMapAsync(this)
        googleMapView.onCreate(arguments)
        setupRecyclerView()

        retryButton.setOnClickListener {
            retryLoadEventIntentPublisher.onNext(RetryLoadEventIntent(args.eventId))
        }
    }

    private fun setupRecyclerView() {
        sourceRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = SourceAdapter(mutableListOf(), this)
        sourceRecyclerView.adapter = adapter
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

    override fun onStart() {
        super.onStart()
        googleMapView.onStart()
        bindViewModel()
    }

    override fun onStop() {
        super.onStop()
        googleMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        googleMapView?.onDestroy()
    }

    private fun bindViewModel() {
        viewModel.states().observe(this, Observer<EventDetailViewState> {
            render(it)
        })
        viewModel.processIntents(intents())
    }

    private fun mapReadyIntent(): Observable<MapReadyIntent> {
        return mapReadyIntentPublisher
    }

    private fun retryLoadEventIntent(): Observable<RetryLoadEventIntent> {
        return retryLoadEventIntentPublisher
    }

    override fun intents(): Observable<EventDetailIntent> {
        return Observable.merge(
            Observable.just(LoadEventDetailIntent(args.eventId)),
            mapReadyIntent(),
            retryLoadEventIntent()
        )
    }

    override fun render(state: EventDetailViewState) {
        when (state) {
            is EventDetailViewState.LoadingView -> {
                makeInVisible(eventNotExistView, errorView, dataView)
                loadingView.makeVisible()
            }
            is EventDetailViewState.EventNotExistView -> {
                makeInVisible(loadingView, errorView, dataView)
                eventNotExistView.makeVisible()
            }
            is EventDetailViewState.ErrorView -> {
                makeInVisible(loadingView, eventNotExistView, dataView)
                errorView.makeVisible()

                Toast.makeText(
                    context,
                    getString(R.string.error_loading_events),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Error loading event detail: ${state.message}")
            }
            is EventDetailViewState.DataView -> {
                makeInVisible(loadingView, eventNotExistView, errorView)
                dataView.makeVisible()

                //Update title
                title.text = state.event.title

                if (state.event.categories.isNotEmpty()) {
                    category.makeVisible()
                    var categoryData = state.event.categories.joinToString(" #") { it.title }
                    categoryData = "#$categoryData"
                    category.text = categoryData

                } else {
                    category.makeInVisible()
                }

                //Setup source recycler view
                if (state.event.sources.isNotEmpty()) {
                    makeVisible(sourceTitle, sourceRecyclerView)
                    adapter.updateSourceList(state.event.sources)
                } else {
                    makeInVisible(sourceTitle, sourceRecyclerView)
                }

                //Description
                if (state.event.description.isNotEmpty()) {
                    makeVisible(descriptionTitle, description)
                    description.text = state.event.description
                } else {
                    makeInVisible(descriptionTitle, description)
                }

                //Map view
                if (state.event.geometries.isNotEmpty()) {
                    makeVisible(googleMapView, locationTitle)
                    updateGeometryOnMap(state.event.geometries)
                } else {
                    makeInVisible(googleMapView, locationTitle)
                }
            }
        }
    }

    override fun onSourceSelected(source: EOSource) {
        val direction =
            EventDetailFragmentDirections.actionEventDetailFragmentToWebContentFragment(source.url)
        findNavController().navigate(direction)
    }

    override fun onMapReady(map: GoogleMap?) {
        readyMap = map
        readyMap?.let {
            it.uiSettings.isZoomControlsEnabled = true
            it.uiSettings.isZoomGesturesEnabled = true
            it.uiSettings.isScrollGesturesEnabled = true
            it.uiSettings.isMapToolbarEnabled = true

            mapReadyIntentPublisher.onNext(MapReadyIntent)

            it.setOnMarkerClickListener { _it ->
                _it?.let { marker ->
                    if (marker.isInfoWindowShown) {
                        marker.hideInfoWindow()
                    } else {
                        marker.showInfoWindow()
                    }
                }
                true // Marker click is handled
            }
        }
    }

    private fun updateGeometryOnMap(geometry: List<EOBaseGeometry>) {
        val pointGeometries = geometry.filterIsInstance<EOPointGeometry>()
        val polygonGeometries = geometry.filterIsInstance<EOPolygonGeometry>()

        var position: LatLng? = null
        var zoomLevel = 5.0f
        for (item in pointGeometries) {
            val marker = readyMap?.addMarker(convertToMarker(item))
            position = marker?.position
            zoomLevel = 5.0f
            marker?.showInfoWindow()
        }

        for (item in polygonGeometries) {
            val polygonOptions = convertToPolygon(item)
            for (polygonOption in polygonOptions) {
                val polygon = readyMap?.addPolygon(polygonOption)
                position = polygon?.points?.get(0)
                zoomLevel = 10.0f
            }
        }

        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        position?.let {
            val cameraPosition = CameraPosition.Builder()
                .target(position)      // Sets the center of the map to Mountain View
                .zoom(zoomLevel)                   // Sets the zoom
                .build()
            readyMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun convertToMarker(geometry: EOPointGeometry): MarkerOptions {
        return MarkerOptions()
            .position(
                /**
                 * In GeoGson format:
                 * Point coordinates are in x, y order (easting, northing for projected
                 * coordinates, longitude, and latitude for geographic coordinates):
                 * Ex. {
                 *       "type": "Point",
                 *       "coordinates": [100.0, 0.0]
                 *      }
                 *
                 * LatLong verification for google map:
                 * List your latitude coordinates before longitude coordinates.
                 * Check that the first number in your latitude coordinate is between -90 and 90.
                 * Check that the first number in your longitude coordinate is between -180 and 180.
                 */
                LatLng(geometry.coordinates[1], geometry.coordinates[0])
            )
            .title(geometry.date.formattedDateTime())
    }

    private fun convertToPolygon(geometry: EOPolygonGeometry): List<PolygonOptions> {
        val polygonOptions = mutableListOf<PolygonOptions>()
        for (parentArray in geometry.coordinates) {
            val polygonOption = PolygonOptions()
            for (item in parentArray) {
                val latLong = LatLng(item[1], item[0])
                polygonOption.add(latLong)
            }
            polygonOptions.add(polygonOption)
        }
        return polygonOptions
    }
}
