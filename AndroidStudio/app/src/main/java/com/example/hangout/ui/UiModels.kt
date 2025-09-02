package com.example.hangout.ui.models

data class EventoUI(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val fechaIso: String,
    val precio: Double,
    val hora: String,
    val idEstablecimiento: String,
    val nombreEstablecimiento: String,
    val imagenUrl: String
)

data class ParticipanteUI(
    val id: String,
    val nombre_usuario: String,
    val imagen_url: String
)

data class ActividadUI(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val fechaIso: String,
    val hora: String,
    val ubicacion: String,
    val idCreador: String,
    val perfil_participantes: List<ParticipanteUI>
)
