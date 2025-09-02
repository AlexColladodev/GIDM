package com.example.hangout.models

data class ParticipanteItem(
    val id: String,
    val nombreUsuario: String,
    val imagenUrl: String
)

data class ActividadItem(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val fechaIso: String,
    val hora: String,
    val ubicacion: String,
    val idCreador: String,
    val perfilParticipantes: List<ParticipanteItem> = emptyList()
)
