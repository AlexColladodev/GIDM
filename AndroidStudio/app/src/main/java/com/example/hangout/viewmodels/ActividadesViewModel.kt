package com.example.hangout.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangout.models.Actividad
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActividadesViewModel : ViewModel() {
    private val _actividades = MutableStateFlow<List<Actividad>>(emptyList())
    val actividades: StateFlow<List<Actividad>> = _actividades

    fun cargarActividades() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getActividades()
                if (response.isSuccessful) {
                    _actividades.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
