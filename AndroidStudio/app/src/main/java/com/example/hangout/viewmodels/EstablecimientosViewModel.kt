package com.example.hangout.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangout.models.Establecimiento
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EstablecimientosViewModel : ViewModel() {

    private val _establecimientos = MutableStateFlow<List<Establecimiento>>(emptyList())
    val establecimientos: StateFlow<List<Establecimiento>> = _establecimientos

    fun cargarEstablecimientos() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getEstablecimientos()
                if (response.isSuccessful) {
                    _establecimientos.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
