package com.example.hangout.models

data class PerfilResponse(
    val usuario: UsuarioGenerico,
    val actividades: List<Actividad>,
    val reviews: List<Review>
)
