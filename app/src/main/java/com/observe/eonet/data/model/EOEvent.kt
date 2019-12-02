package com.observe.eonet.data.model

data class EOEvent(
    val id: String,
    val title: String,
    val categories: List<EOCategory>,
    val sources: List<EOSource>,
    val geometries: List<EOBaseGeometry>
)

data class EOSource(
    val id: String,
    val url: String
)

sealed class EOBaseGeometry {
    data class EOPointGeometry(
        val date: String,
        val type: String,
        val coordinates: Array<Float>
    ) : EOBaseGeometry()

    data class EOPolygonGeomatry(
        val date: String,
        val type: String,
        val coordinates: Array<Array<Float>>
    ) : EOBaseGeometry()
}