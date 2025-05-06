package com.example.hangout.models

data class AdministradorEstablecimiento(
    val nombre: String,
    val nombre_usuario: String,
    val password: String,
    val email: String,
    val establecimientos: List<String>,
    val dni: String,
    val telefono: String,
    val fecha_nac: String,
    val imagen_url: String
)
