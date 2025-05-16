package com.example.hangout.models

data class Establecimiento(
    val _id: IdWrapper?,
    val cif: String,
    val nombre_establecimiento: String,
    val id_administrador: String,
    val ambiente: List<String>,
    val ofertas: List<String>,
    val eventos: List<String>,
    val reviews: List<String>,
    val imagen_url: String
) {
    val id: String
        get() = _id?.`$oid` ?: ""
}

data class IdWrapper(
    val `$oid`: String
)
