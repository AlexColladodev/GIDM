package com.example.hangout.viewmodels

import android.content.Context
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

class UsuariosGenericosViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioUsuarioUiState())
    val uiState: StateFlow<InicioUsuarioUiState> = _uiState

    init {
        cargarDatosInicio()
    }

    private fun cargarDatosInicio() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(loading = true)

                val api = RetrofitInstance.create(context)

                val idsResponse = api.getEstablecimientos()
                val ids = if (idsResponse.isSuccessful) idsResponse.body() ?: emptyList() else emptyList()

                val preferidos = ids.mapNotNull { id ->
                    val response = api.getEstablecimientoById(id)
                    if (response.isSuccessful) response.body() else null
                }

                val eventos = try {
                    val res = api.getEventos()
                    if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
                } catch (_: Exception) {
                    emptyList()
                }

                val actividades = try {
                    val res = api.getActividades()
                    if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
                } catch (_: Exception) {
                    emptyList()
                }



            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}
