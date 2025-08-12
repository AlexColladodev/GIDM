package com.example.hangout.ui.state

import com.example.hangout.models.Establecimiento
import com.example.hangout.models.Evento
import com.example.hangout.models.Actividad
import com.example.hangout.models.Ambiente

data class InicioUsuarioUiState(
    val loading: Boolean = true,
    val preferidos: List<Establecimiento> = emptyList(),
    val establecimientos: List<Establecimiento> = emptyList(),
    val eventos: List<Evento> = emptyList(),
    val actividades: List<Actividad> = emptyList(),
    val filtrados: List<Establecimiento> = emptyList(),
    val ambientes: List<Ambiente> = emptyList(),
    val ambientesSeleccionados: List<Ambiente> = emptyList()
)
