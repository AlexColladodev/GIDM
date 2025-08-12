package com.example.hangout.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.hangout.models.Oferta
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosOfertaScreen(navController: NavController) {
    val oferta = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<Oferta>("oferta_arg")

    val establecimientoNombre = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("establecimiento_nombre_arg") ?: "Establecimiento"

    val ctx = LocalContext.current
    val img = buildImageUrl(oferta?.imagen_url)
    val precio = oferta?.precio_oferta?.let { String.format(Locale.getDefault(), "%.0f €", it) } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oferta en $establecimientoNombre", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
                Text(oferta?.nombre_oferta.orEmpty(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(precio, style = MaterialTheme.typography.titleLarge)
            }
            if (!oferta?.descripcion_oferta.isNullOrBlank()) {
                Text(oferta?.descripcion_oferta.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = { /* canjear oferta */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50)
            ) { Text("Usar oferta") }
        }
    }
}

/* Helpers locales */
private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize(BASE_URL.trimEnd('/') + path)
}
private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
private const val BASE_URL = "http://127.0.0.1:5000"
