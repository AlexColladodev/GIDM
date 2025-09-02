package com.example.hangout.models

data class UsuarioPerfil(
    val id: String,
    val nombre: String,
    val nombreUsuario: String,
    val email: String,
    val telefono: String,
    val fechaNacIso: String,
    val imagenUrl: String,
    val preferencias: List<String>
)

data class ParticipantePerfil(
    val nombreUsuario: String,
    val imagenUrl: String
)

data class ActividadPerfil(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val fechaIso: String,
    val hora: String,
    val ubicacion: String,
    val participantes: List<ParticipantePerfil>
)

data class ReviewPerfil(
    val id: String,
    val calificacion: Double,
    val mensaje: String,
    val nombreEstablecimiento: String,
    val fechaCreacionIso: String
)
