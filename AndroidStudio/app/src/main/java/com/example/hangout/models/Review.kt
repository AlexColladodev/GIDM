package com.example.hangout.models

data class Review(
    val _id: IdWrapper?,
    val calificacion: Float,
    val mensaje: String,
    val id_usuario: String,
    val id_establecimiento: String,
    val fecha_creacion: String
)
