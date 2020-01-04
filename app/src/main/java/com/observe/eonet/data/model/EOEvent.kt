package com.observe.eonet.data.model

import java.util.*

data class EOEvent(
    val id: String,
    val title: String,
    val description: String,
    val categories: List<EOCategory>,
    val sources: List<EOSource>,
    val geometries: List<EOBaseGeometry>,
    val isClosed: Boolean?,
    val startDate: Date?
)

data class EOSource(
    val id: String,
    val url: String
)

sealed class EOBaseGeometry {
    data class EOPointGeometry(
        val date: String,
        val type: String,
        val coordinates: Array<Double>
    ) : EOBaseGeometry() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EOPointGeometry

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

    data class EOPolygonGeometry(
        val date: String,
        val type: String,
        val coordinates: Array<Array<Array<Double>>>
    ) : EOBaseGeometry() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EOPolygonGeometry

            if (date != other.date) return false
            if (type != other.type) return false
            if (!coordinates.contentDeepEquals(other.coordinates)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = date.hashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + coordinates.contentDeepHashCode()
            return result
        }

    }
}