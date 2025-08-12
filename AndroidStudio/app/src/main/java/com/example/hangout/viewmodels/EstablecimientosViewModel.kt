package com.example.hangout.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangout.models.Establecimiento
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EstablecimientosViewModel(private val context: Context) : ViewModel() {

    private val _establecimientos = MutableStateFlow<List<Establecimiento>>(emptyList())
    val establecimientos: StateFlow<List<Establecimiento>> = _establecimientos

    fun cargarEstablecimientos() {
        viewModelScope.launch {
            try {
                val api = RetrofitInstance.create(context)
                val responseIds = api.getEstablecimientos()
                if (responseIds.isSuccessful) {
                    val ids = responseIds.body() ?: emptyList()
                    val detalles = ids.mapNotNull { id ->
                        try {
                            val detalleResponse = api.getEstablecimientoById(id)
                            if (detalleResponse.isSuccessful) detalleResponse.body() else null
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    _establecimientos.value = detalles
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
