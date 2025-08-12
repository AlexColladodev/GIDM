package com.example.hangout.models

data class Oferta(
    val _id: IdWrapper?,
    val nombre_oferta: String,
    val descripcion_oferta: String,
    val precio_oferta: Float,
    val id_establecimiento: String,
    val imagen_url: String
)

