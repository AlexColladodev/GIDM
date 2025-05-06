package com.example.hangout.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangout.models.AdministradorEstablecimiento
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdministradoresEstablecimientosViewModel : ViewModel() {
    private val _administradores = MutableStateFlow<List<AdministradorEstablecimiento>>(emptyList())
    val administradores: StateFlow<List<AdministradorEstablecimiento>> = _administradores

    fun cargarAdministradores() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAdministradores()
                if (response.isSuccessful) {
                    _administradores.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
