package com.example.hangout.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangout.models.*
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InicioUsuarioUiState(
    val loading: Boolean = true,
    val preferidos: List<Establecimiento> = emptyList(),
    val eventos: List<Evento> = emptyList(),
    val actividades: List<Actividad> = emptyList()
)

class UsuariosGenericosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUsuarioUiState())
    val uiState: StateFlow<InicioUsuarioUiState> = _uiState

    init {
        cargarDatosInicio()
    }

    private fun cargarDatosInicio() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(loading = true)

                val idsResponse = RetrofitInstance.api.getEstablecimientos()
                val ids = if (idsResponse.isSuccessful) idsResponse.body() ?: emptyList() else emptyList()

                val preferidos = ids.mapNotNull { id ->
                    val response = RetrofitInstance.api.getEstablecimientoById(id)
                    if (response.isSuccessful) response.body() else null
                }

                val eventos = try {
                    val res = RetrofitInstance.api.getEventos()
                    if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
                } catch (_: Exception) {
                    emptyList()
                }

                val actividades = try {
                    val res = RetrofitInstance.api.getActividades()
                    if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
                } catch (_: Exception) {
                    emptyList()
                }

                _uiState.value = InicioUsuarioUiState(
                    loading = false,
                    preferidos = preferidos,
                    eventos = eventos,
                    actividades = actividades
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}
