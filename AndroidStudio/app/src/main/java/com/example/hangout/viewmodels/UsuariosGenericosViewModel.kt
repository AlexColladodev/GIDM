package com.example.hangout.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangout.models.UsuarioGenerico
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsuariosGenericosViewModel : ViewModel() {
    private val _usuario = MutableStateFlow<UsuarioGenerico?>(null)
    val usuario: StateFlow<UsuarioGenerico?> = _usuario

    fun cargarPerfil() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPerfil()
                if (response.isSuccessful) {
                    _usuario.value = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
