package com.example.hangout.models

data class Actividad(
    val nombre_actividad: String,
    val descripcion_actividad: String,
    val ubicacion: String,
    val fecha_actividad: String,
    val hora_actividad: String,
    val id_usuario_creador: String,
    val participantes: List<String>
)
