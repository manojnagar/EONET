package com.observe.eonet.data.model

data class EOEventResponse(
    val title: String,
    val description: String,
    val link: String,
    val events: List<EOEvent>
)