package com.example.hangout.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangout.models.Oferta
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OfertasViewModel : ViewModel() {
    private val _ofertas = MutableStateFlow<List<Oferta>>(emptyList())
    val ofertas: StateFlow<List<Oferta>> = _ofertas

    fun cargarOfertas() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getOfertas()
                if (response.isSuccessful) {
                    _ofertas.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
