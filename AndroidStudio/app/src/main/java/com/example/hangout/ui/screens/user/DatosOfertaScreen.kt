package com.example.hangout.ui.screens.usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
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
import com.example.hangout.models.Oferta
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosOfertaScreen(navController: NavController) {
    val stableHandle = remember { navController.previousBackStackEntry?.savedStateHandle }
    val ofertaJson = remember { stableHandle?.get<String>("oferta_json") }
    val otrasJson  = remember { stableHandle?.get<String>("otras_ofertas_json") }
    val establecimientoNombre = remember {
        stableHandle?.get<String>("establecimiento_nombre_arg") ?: "Establecimiento"
    }

    val oferta = remember(ofertaJson) { ofertaJson?.let { Gson().fromJson(it, Oferta::class.java) } }
    val otras: List<Oferta> = remember(otrasJson) {
        if (otrasJson.isNullOrBlank()) emptyList()
        else Gson().fromJson(otrasJson, object : com.google.gson.reflect.TypeToken<List<Oferta>>() {}.type)
    }
    val gson = remember { Gson() }
    val ctx = LocalContext.current
    val img = buildImageUrl(oferta?.imagen_url)
    val precio = oferta?.precio_oferta?.let { String.format(Locale.getDefault(), "%.0f €", it) } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oferta en $establecimientoNombre", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = { IconButton(onClick = { }) { Icon(Icons.Filled.Share, null) } }
            )
        }
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(img).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
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
                if (precio.isNotBlank()) {
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

            Text(
                text = oferta?.nombre_oferta.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(establecimientoNombre) },
                    leadingIcon = { Icon(Icons.Filled.Place, null) },
                    shape = RoundedCornerShape(50)
                )
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Descripción", style = MaterialTheme.typography.titleMedium)
                    Text(
                        oferta?.descripcion_oferta.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }
            }


            if (otras.isNotEmpty()) {
                Text("Más ofertas", style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(otras) { o ->
                        OfertaMiniCard(
                            oferta = o,
                            onClick = {
                                val nuevaSel = gson.toJson(o)
                                val nuevasOtras = buildList {
                                    otras.filter { it != o }.forEach { add(it) }
                                    oferta?.let { add(it) }
                                }
                                val nuevasOtrasJson = gson.toJson(nuevasOtras)
                                val back = navController.previousBackStackEntry
                                back?.savedStateHandle?.set("oferta_json", nuevaSel)
                                back?.savedStateHandle?.set("otras_ofertas_json", nuevasOtrasJson)
                                back?.savedStateHandle?.set("establecimiento_nombre_arg", establecimientoNombre)
                                navController.navigate("datos_oferta") { launchSingleTop = true }

                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfertaMiniCard(oferta: Oferta, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            val ctx = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(buildImageUrl(oferta.imagen_url)).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
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
                    oferta.nombre_oferta.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val precio = oferta.precio_oferta?.let { String.format(Locale.getDefault(), "%.0f €", it) } ?: ""
                if (precio.isNotBlank()) {
                    Text(precio, style = MaterialTheme.typography.labelLarge)
                }
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
