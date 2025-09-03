package com.example.hangout.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hangout.models.Evento
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosEventosScreen(navController: NavController, id: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var evento by remember { mutableStateOf<Evento?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        try {
            val api = RetrofitInstance.create(context)
            val resp = api.getEventoById(id)
            evento = resp.body()
        } finally {
            loading = false
        }
    }

    val titulo = evento?.nombre_evento ?: "Evento"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("editar_evento/$id") }) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Cargando…") }
        } else if (evento == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("No se encontró el evento") }
        } else {
            val e = evento!!
            val img = buildImageUrl(e.imagen_url ?: "/_uploads/photos/default_establecimiento.png")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(img).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                Text(e.nombre_evento ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

                OutlinedCard(shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        KeyValueRow("Fecha", formatDate(e.fecha_evento))
                        KeyValueRow("Hora", e.hora_evento ?: "—")
                        KeyValueRow("Precio", formatPrice(e.precio))
                        KeyValueRow("Descripción", e.descripcion_evento ?: "—")
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar evento") },
            text = { Text("¿Seguro que deseas eliminar este evento? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            try {
                                val api = RetrofitInstance.create(context)
                                val resp = api.deleteEvento(id)
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, "No se pudo eliminar", Toast.LENGTH_LONG).show()
                                }
                            } catch (ex: Exception) {
                                Toast.makeText(context, "Error: ${ex.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun KeyValueRow(key: String, value: String?) {
    Column(Modifier.fillMaxWidth()) {
        Text(key, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(value ?: "—", style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatPrice(value: Any?): String {
    val number = when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        else -> null
    } ?: return "—"
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    return nf.format(number)
}

private fun formatDate(value: Any?): String {
    val iso: String = when (value) {
        is String -> value
        is Map<*, *> -> (value["\$date"] ?: value["date"]) as? String ?: return "—"
        else -> {
            val s = value?.toString() ?: return "—"
            val i = s.indexOf("date=")
            if (i >= 0) {
                val start = i + 5
                val end = s.indexOfAny(charArrayOf('}', ',', ')'), start).let { if (it == -1) s.length else it }
                s.substring(start, end).trim().trim('"')
            } else return "—"
        }
    }

    val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss"
    )

    for (p in inputPatterns) {
        try {
            val inFmt = java.text.SimpleDateFormat(p, java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val date = inFmt.parse(iso) ?: continue
            val outFmt = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("es", "ES")).apply {
                timeZone = java.util.TimeZone.getDefault()
            }
            return outFmt.format(date)
        } catch (_: Exception) { }
    }
    return iso
}


/*
private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("http://127.0.0.1:5000/_uploads/photos/default_establecimiento.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize("http://127.0.0.1:5000$path")
}*/

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return "http://127.0.0.1:5000/_uploads/photos/default.png"
    if (path.startsWith("http")) return path
    return "http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}"
}


private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
