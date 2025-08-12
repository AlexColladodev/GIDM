package com.example.hangout.models

data class UsuarioGenerico(
    val nombre: String,
    val nombre_usuario: String,
    val password: String,
    val email: String,
    val telefono: String,
    val seguidos: List<String>,
    val preferencias: List<String>,
    val actividades_creadas: List<String>,
    val reviews: List<String>,
    val fecha_nac: String,
    val imagen_url: String
)
