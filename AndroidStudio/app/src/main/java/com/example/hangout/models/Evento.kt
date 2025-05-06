package com.example.hangout.models

data class Evento(
    val _id: IdWrapper?,
    val nombre_evento: String,
    val descripcion_evento: String,
    val fecha_evento: FechaWrapper?,
    val precio: Float,
    val hora_evento: String,
    val id_establecimiento: String,
    val imagen_url: String
)

data class FechaWrapper(
    val `$date`: String
)
