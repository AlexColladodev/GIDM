package com.example.hangout.ui.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Esta pantalla NO llama a la API.
 * Lee el JSON del evento desde SavedStateHandle con la clave "evento_json"
 * (el JSON lo envía la pantalla anterior).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosEventoScreen2(
    navController: NavController
) {
    val gson = remember { Gson() }
    val backEntry = navController.previousBackStackEntry ?: navController.currentBackStackEntry
    val eventoJson = backEntry?.savedStateHandle?.get<String>("evento_json")

    val evento: EventoUI? = remember(eventoJson) {
        eventoJson?.let { gson.fromJson(it, EventoUI::class.java) }
    }

    val ctx = LocalContext.current
    val imgUrl = buildImageUrl(evento?.imagenUrl)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(evento?.nombre ?: "Evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (evento == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("No se pudo cargar el evento.") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(imgUrl).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.6f to Color.Transparent,
                                1f to Color(0x22000000)
                            )
                        )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    evento.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        evento.nombreEstablecimiento.ifBlank { "Establecimiento" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        formatDate(evento.fechaIso),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (evento.descripcion.isNotBlank()) {
                Text(
                    evento.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (evento.precio > 0.0) {
                AssistChip(
                    onClick = { /* reservado para más adelante */ },
                    label = { Text("${evento.precio} €") }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Modelo que esperamos recibir desde la lista (pásalo tal cual en JSON). */
data class EventoUI(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val fechaIso: String,
    val precio: Double,
    val hora: String,
    val idEstablecimiento: String,
    val nombreEstablecimiento: String,
    val imagenUrl: String
)

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default_establecimiento.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize(BASE_URL.trimEnd('/') + path)
}
private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")

private fun formatDate(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inFmt.timeZone = TimeZone.getTimeZone("UTC")
        val date = inFmt.parse(iso)
        val outFmt = SimpleDateFormat("EEE, d MMM yyyy • HH:mm", Locale.getDefault())
        outFmt.format(date!!)
    } catch (_: Exception) {
        iso
    }
}

private const val BASE_URL = "http://127.0.0.1:5000"
