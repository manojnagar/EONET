package com.observe.eonet.data.repository.remote

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.observe.eonet.data.model.EOBaseGeometry
import com.observe.eonet.data.model.EOBaseGeometry.EOPointGeometry
import com.observe.eonet.data.model.EOBaseGeometry.EOPolygonGeometry
import java.lang.reflect.Type

class GeometryDeserializer : JsonDeserializer<EOBaseGeometry> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EOBaseGeometry {
        val type = json?.asJsonObject?.get("type")?.asString ?: "Point"
        return if (type == "Point") {
            context!!.deserialize<EOPointGeometry>(json, EOPointGeometry::class.java)
        } else {
            context!!.deserialize<EOPolygonGeometry>(
                json,
                EOPolygonGeometry::class.java
            )
        }
    }
}