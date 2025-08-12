package com.example.hangout.models

import com.google.gson.annotations.SerializedName

data class Evento(
    val _id: IdWrapper?,
    val nombre_evento: String,
    val descripcion_evento: String,
    val fecha_evento: FechaWrapper,
    val precio: Float,
    val hora_evento: String,
    val id_establecimiento: String,
    val imagen_url: String,
    val nombre_establecimiento: String
) {
    val id: String get() = _id?.`$oid` ?: ""
}

data class FechaWrapper(
    @SerializedName("\$date")
    val date: String
)
