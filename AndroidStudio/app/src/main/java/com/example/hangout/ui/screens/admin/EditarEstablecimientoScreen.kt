package com.example.hangout.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hangout.data.AmbientesProvider
import com.example.hangout.models.Ambiente
import com.example.hangout.models.Establecimiento
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.text.Normalizer
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarEstablecimientoScreen(
    navController: NavController,
    idEstablecimiento: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var est by remember { mutableStateOf<Establecimiento?>(null) }

    var nombre by remember { mutableStateOf(TextFieldValue("")) }
    var selectedAmbientes by remember { mutableStateOf(setOf<Ambiente>()) }

    LaunchedEffect(idEstablecimiento) {
        try {
            val api = RetrofitInstance.create(context)
            val resp = api.getEstablecimientoById(idEstablecimiento)
            if (resp.isSuccessful && resp.body() != null) {
                est = resp.body()
                nombre = TextFieldValue(est!!.nombre_establecimiento)
                val currentKeys = est!!.ambiente.map { normalizeText(it) }.toSet()
                val all = AmbientesProvider.listaAmbientes
                selectedAmbientes = all.filter { normalizeText(it.name) in currentKeys }.toSet()
            } else {
                Toast.makeText(context, "No se pudo cargar el establecimiento", Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar establecimiento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Cargando…")
            }
        } else {
            val e = est ?: return@Scaffold
            val img = buildImageUrl(e.imagen_url)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(img).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del establecimiento") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Ambiente", style = MaterialTheme.typography.titleMedium)
                AmbientesGrid(
                    selected = selectedAmbientes,
                    onToggle = { amb ->
                        selectedAmbientes = if (amb in selectedAmbientes) selectedAmbientes - amb else selectedAmbientes + amb
                    }
                )

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val current = est ?: return@launch
                                val nuevoNombre = nombre.text.trim()
                                if (nuevoNombre.isBlank()) {
                                    Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val nuevosAmbientes = selectedAmbientes.map { normalizeText(it.name) }

                                val updated = Establecimiento(
                                    _id = current._id,
                                    cif = current.cif,
                                    nombre_establecimiento = nuevoNombre,
                                    id_administrador = current.id_administrador,
                                    ambiente = nuevosAmbientes,
                                    ofertas = current.ofertas,
                                    eventos = current.eventos,
                                    reviews = current.reviews,
                                    imagen_url = current.imagen_url
                                )

                                val api = RetrofitInstance.create(context)
                                val resp = api.updateEstablecimiento(idEstablecimiento, updated)
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Establecimiento actualizado", Toast.LENGTH_SHORT).show()
                                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_establecimiento", true)
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "No se pudo actualizar", Toast.LENGTH_LONG).show()
                                }
                            } catch (ex: Exception) {
                                Toast.makeText(context, "Error: ${ex.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar cambios")
                }
            }
        }
    }
}

@Composable
private fun AmbientesGrid(
    selected: Set<Ambiente>,
    onToggle: (Ambiente) -> Unit
) {
    val ambientes = AmbientesProvider.listaAmbientes
    val filas = ambientes.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        filas.forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                fila.forEach { amb ->
                    val seleccionado = amb in selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable { onToggle(amb) }
                    ) {
                        Surface(
                            tonalElevation = if (seleccionado) 4.dp else 0.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = amb.imageRes),
                                contentDescription = amb.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .padding(8.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(amb.name, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
/*
private fun buildImageUrl(path: String): String {
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize("http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}")
}*/

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return "http://127.0.0.1:5000/_uploads/photos/default.png"
    if (path.startsWith("http")) return path
    return "http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}"
}


private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")

private fun normalizeText(s: String): String {
    val n = Normalizer.normalize(s.lowercase().trim(), Normalizer.Form.NFD)
    return n.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}
