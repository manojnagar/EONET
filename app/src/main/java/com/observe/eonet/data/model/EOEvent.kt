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
    val type: String,
    val coordinates: Array<Float>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EOGeometry

        if (date != other.date) return false
        if (type != other.type) return false
        if (!coordinates.contentEquals(other.coordinates)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + coordinates.contentHashCode()
        return result
    }
}