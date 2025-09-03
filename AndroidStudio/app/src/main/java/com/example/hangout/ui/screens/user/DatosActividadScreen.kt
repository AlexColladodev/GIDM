package com.example.hangout.ui.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hangout.ui.components.CircleBackground
import com.example.hangout.ui.models.ActividadUI
import com.example.hangout.ui.models.ParticipanteUI
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosActividadScreen(navController: NavController) {
    val rawFromPrev = navController.previousBackStackEntry?.savedStateHandle?.get<String>("actividad_json")
    val rawFromCurr = navController.currentBackStackEntry?.savedStateHandle?.get<String>("actividad_json")
    val json = rawFromPrev ?: rawFromCurr ?: ""
    val actividadOrNull = remember(json) { runCatching { Gson().fromJson(json, ActividadUI::class.java) }.getOrNull() }

    Box(Modifier.fillMaxSize()) {
        CircleBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(actividadOrNull?.nombre ?: "Actividad") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            if (actividadOrNull == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se pudo cargar la actividad")
                }
            } else {
                val actividad = actividadOrNull
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = actividad.nombre,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            FechaUbicacionSection(
                                fechaTexto = formatDate(actividad.fechaIso) + " • " + actividad.hora,
                                ubicacion = actividad.ubicacion
                            )
                            Text(
                                text = actividad.descripcion,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }
                    }

                    Text(
                        text = "Participantes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (actividad.perfil_participantes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            Text("Aún no hay participantes")
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(actividad.perfil_participantes, key = { it.id }) { p ->
                                ParticipanteChip(p)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FechaUbicacionSection(fechaTexto: String, ubicacion: String) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 2
    ) {
        InfoBadge(icon = Icons.Filled.CalendarToday, text = fechaTexto)
        InfoBadge(icon = Icons.Filled.Place, text = ubicacion.ifBlank { "Ubicación" })
    }
}

@Composable
private fun InfoBadge(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(min = 0.dp, max = 520.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                softWrap = true,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ParticipanteChip(p: ParticipanteUI) {
    Column(
        modifier = Modifier
            .widthIn(min = 84.dp)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = buildImageUrl(p.imagen_url),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
        )
        Text(
            text = p.nombre_usuario,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDate(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inFmt.timeZone = TimeZone.getTimeZone("UTC")
        val date = inFmt.parse(iso)
        val outFmt = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        outFmt.format(date!!)
    } catch (_: Exception) {
        iso
    }
}
/*
private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default_user.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize(BASE_URL.trimEnd('/') + path)
}*/
private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return "http://127.0.0.1:5000/_uploads/photos/default_establecimiento.png"
    if (path.startsWith("http")) return path
    return "http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}"
}

private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
private const val BASE_URL = "http://127.0.0.1:5000"
