package com.example.hangout.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hangout.models.Oferta
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarOfertaScreen(
    navController: NavController,
    id: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var oferta by remember { mutableStateOf<Oferta?>(null) }
    var loading by remember { mutableStateOf(true) }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }

    LaunchedEffect(id) {
        try {
            val api = RetrofitInstance.create(context)
            val resp = api.getOfertaById(id)
            val o = resp.body()
            oferta = o
            if (o != null) {
                nombre = o.nombre_oferta ?: ""
                descripcion = o.descripcion_oferta ?: ""
                precioText = o.precio_oferta.toString()
            }
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar oferta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val base = oferta ?: return@IconButton
                        val body = mutableMapOf<String, Any>()

                        val n = nombre.trim()
                        if (n.isNotEmpty() && n != base.nombre_oferta) body["nombre_oferta"] = n

                        val d = descripcion.trim()
                        if (d.isNotEmpty() && d != base.descripcion_oferta) body["descripcion_oferta"] = d

                        val p = precioText.replace(',', '.').toFloatOrNull()
                        if (p == null) {
                            Toast.makeText(context, "Precio inválido", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        } else if (p != base.precio_oferta) {
                            body["precio_oferta"] = p
                        }

                        if (body.isEmpty()) {
                            Toast.makeText(context, "No hay cambios", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        scope.launch {
                            try {
                                val api = RetrofitInstance.create(context)
                                val resp = api.updateOfertaPartial(id, body)
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Oferta actualizada", Toast.LENGTH_SHORT).show()
                                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_oferta", true)
                                    navController.popBackStack()
                                } else {
                                    val err = resp.errorBody()?.string()
                                    Toast.makeText(context, "No se pudo actualizar: $err", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (oferta == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se encontró la oferta")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción") },
                    minLines = 3
                )
                OutlinedTextField(
                    value = precioText,
                    onValueChange = { precioText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}
