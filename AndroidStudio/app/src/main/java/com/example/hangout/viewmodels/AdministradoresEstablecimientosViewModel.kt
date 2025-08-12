package com.example.hangout.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangout.models.AdministradorEstablecimiento
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdministradoresEstablecimientosViewModel(private val context: Context) : ViewModel() {
    private val _administradores = MutableStateFlow<List<AdministradorEstablecimiento>>(emptyList())
    val administradores: StateFlow<List<AdministradorEstablecimiento>> = _administradores

    fun cargarAdministradores() {
        viewModelScope.launch {
            try {
                val api = RetrofitInstance.create(context)
                val response = api.getAdministradores()
                if (response.isSuccessful) {
                    _administradores.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
