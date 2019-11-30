package com.observe.eonet.data.model

data class EOEvent(
    val id: String,
    val title: String,
    val categories: List<EOCategory>,
    val sources: List<EOSource>,
    val geometries: List<EOGeometry>
)

data class EOSource(
    val id: String,
    val url: String
)

data class EOGeometry(
    val date: String,
    val type: String
    //val coordinates: Array<Float>
)