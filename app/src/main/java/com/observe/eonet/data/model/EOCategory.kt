package com.observe.eonet.data.model

data class EOCategory(
    val id: String,
    val title: String,
    val link: String,
    val events: MutableList<EOEvent>?
)