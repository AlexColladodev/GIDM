package com.example.hangout.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hangout.models.Evento
import com.example.hangout.models.FechaWrapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosEventoScreen(navController: NavController) {
    val evento = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<Evento>("evento_arg")

    val establecimientoNombre = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("establecimiento_nombre_arg")
        ?: evento?.nombre_establecimiento
        ?: "Establecimiento"

    val ctx = LocalContext.current
    val img = buildImageUrl(evento?.imagen_url)
    val precio = String.format(Locale.getDefault(), "%.0f €", evento?.precio ?: 0f)
    val fechaTxt = formatFecha(evento?.fecha_evento)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evento en $establecimientoNombre", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { /* compartir */ }) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                    }
                }
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(img).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(evento?.nombre_evento.orEmpty(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(precio, style = MaterialTheme.typography.titleLarge)
            }
            if (fechaTxt.isNotBlank()) {
                Row {
                    AssistChip(
                        onClick = {},
                        label = { Text(fechaTxt) },
                        leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }
            if (!evento?.descripcion_evento.isNullOrBlank()) {
                Text(evento?.descripcion_evento.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = { /* asistir */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50)
            ) { Text("Asistir") }
        }
    }
}

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize(BASE_URL.trimEnd('/') + path)
}
private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
private const val BASE_URL = "http://127.0.0.1:5000"

private fun formatFecha(f: FechaWrapper?): String {
    val raw = f?.date ?: return ""
    return if (raw.all { it.isDigit() }) {
        val ms = raw.toLongOrNull() ?: return ""
        val d = Date(ms)
        val out = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
        out.format(d)
    } else {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val d = sdf.parse(raw)
                val out = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
                return out.format(d!!)
            } catch (_: Exception) {}
        }
        raw
    }
}
