package com.example.hangout.ui.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
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
import com.example.hangout.models.Evento
import com.example.hangout.models.FechaWrapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DatosEventoScreen(navController: NavController) {
    val stableHandle = remember { navController.previousBackStackEntry?.savedStateHandle }
    val eventoJson = remember { stableHandle?.get<String>("evento_json") }
    val otrosJson  = remember { stableHandle?.get<String>("otros_eventos_json") }
    val establecimientoNombre = remember {
        stableHandle?.get<String>("establecimiento_nombre_arg") ?: "Establecimiento"
    }

    val evento = remember(eventoJson) { eventoJson?.let { Gson().fromJson(it, Evento::class.java) } }
    val otros: List<Evento> = remember(otrosJson) {
        if (otrosJson.isNullOrBlank()) emptyList()
        else Gson().fromJson(otrosJson, object : com.google.gson.reflect.TypeToken<List<Evento>>() {}.type)
    }

    val gson = remember { Gson() }
    val ctx = LocalContext.current
    val img = buildImageUrl(evento?.imagen_url)
    val precio = String.format(Locale.getDefault(), "%.0f €", evento?.precio ?: 0f)
    val ms = remember(evento?.fecha_evento) { parseFechaMillis(evento?.fecha_evento) }
    val fechaChip = remember(ms) { formatFechaCorta(ms) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evento en $establecimientoNombre", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = { IconButton(onClick = { }) { Icon(Icons.Filled.Share, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(img).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.55f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.25f)
                                )
                            )
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            precio,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Text(
                    evento?.nombre_evento.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(establecimientoNombre) },
                        leadingIcon = { Icon(Icons.Filled.Place, null) },
                        shape = RoundedCornerShape(50)
                    )
                    if (fechaChip.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(fechaChip) },
                            leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                    if (!evento?.hora_evento.isNullOrBlank()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(evento?.hora_evento.orEmpty()) },
                            leadingIcon = { Icon(Icons.Filled.Schedule, null) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            item {
                BoxWithConstraints {
                    val compact = maxWidth < 380.dp
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (compact) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Descripción", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    evento?.descripcion_evento.orEmpty(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                )
                                Row(Modifier.fillMaxWidth()) {
                                    Spacer(Modifier.weight(1f))
                                    Button(
                                        onClick = { },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.height(48.dp)
                                    ) { Text("ASISTIR") }
                                }
                            }
                        } else {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(
                                    Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Descripción", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        evento?.descripcion_evento.orEmpty(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                    )
                                }
                                Button(
                                    onClick = { },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(48.dp)
                                ) { Text("ASISTIR") }
                            }
                        }
                    }
                }
            }

            if (otros.isNotEmpty()) {
                item {
                    Text("Más eventos", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 4.dp)
                    ) {
                        items(otros) { e ->
                            EventoMiniCard(
                                evento = e,
                                onClick = {
                                    val nuevaSel = gson.toJson(e)
                                    val nuevosOtros = buildList {
                                        otros.filter { it != e }.forEach { add(it) }
                                        evento?.let { add(it) }
                                    }
                                    val nuevosOtrosJson = gson.toJson(nuevosOtros)
                                    val back = navController.previousBackStackEntry
                                    back?.savedStateHandle?.set("evento_json", nuevaSel)
                                    back?.savedStateHandle?.set("otros_eventos_json", nuevosOtrosJson)
                                    back?.savedStateHandle?.set("establecimiento_nombre_arg", establecimientoNombre)
                                    navController.navigate("datos_evento") { launchSingleTop = true }

                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventoMiniCard(evento: Evento, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .widthIn(min = 180.dp, max = 260.dp)
            .height(140.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            val ctx = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(buildImageUrl(evento.imagen_url)).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentScale = ContentScale.Crop
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    evento.nombre_evento,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val precio = String.format(Locale.getDefault(), "%.0f €", evento.precio)
                Text(precio, style = MaterialTheme.typography.labelLarge)
            }
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

private fun parseFechaMillis(f: FechaWrapper?): Long? {
    val raw = f?.date ?: return null
    return if (raw.all { it.isDigit() }) {
        raw.toLongOrNull()
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
                return sdf.parse(raw)?.time
            } catch (_: Exception) {}
        }
        null
    }
}

private fun formatFechaCorta(ms: Long?): String {
    if (ms == null) return ""
    val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return fmt.format(Date(ms))
}
