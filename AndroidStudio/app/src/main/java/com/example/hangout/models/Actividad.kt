package com.example.hangout.models

import com.google.gson.annotations.SerializedName

data class Actividad(
    val _id: IdWrapper?,
    val nombre_actividad: String,
    val descripcion_actividad: String,
    val fecha_actividad: FechaWrapper,
    val hora_actividad: String,
    val ubicacion: String,
    val id_usuario_creador: String
) {
    val id: String get() = _id?.`$oid` ?: ""
}
