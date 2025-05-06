package com.example.hangout.models

data class Establecimiento(
    val _id: IdWrapper?,
    val cif: String,
    val nombre_establecimiento: String,
    val id_administrador: String,
    val ambiente: List<String>,
    val ofertas: List<Oferta>,
    val eventos: List<Evento>,
    val reviews: List<Review>,
    val imagen_url: String
)

data class IdWrapper(
    val `$oid`: String
)
