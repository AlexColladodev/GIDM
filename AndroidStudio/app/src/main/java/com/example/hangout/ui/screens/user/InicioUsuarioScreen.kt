package com.example.hangout.ui.screens.usuario

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
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
import com.example.hangout.models.Establecimiento
import com.example.hangout.network.RetrofitInstance
import com.example.hangout.ui.components.CircleBackground
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONArray
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioUsuarioScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mejores by remember { mutableStateOf<List<Triple<Establecimiento, Double, Int>>>(emptyList()) }
    var paraTi by remember { mutableStateOf<List<Triple<Establecimiento, Double, Int>>>(emptyList()) }
    var selectedChip by remember { mutableStateOf(0) }
    val chips = listOf("Para ti", "Recientes", "Más visitados", "Low cost")

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val api = RetrofitInstance.create(context)
                val gson = Gson()
                val mejoresResponse = api.getEstablecimientosOrdenados()
                if (mejoresResponse.isSuccessful && mejoresResponse.body() != null) {
                    val raw = mejoresResponse.body()!!.string()
                    val arr = JSONArray(raw)
                    mejores = (0 until arr.length()).map {
                        val sub = arr.getJSONArray(it)
                        val est = gson.fromJson(sub.getJSONObject(0).toString(), Establecimiento::class.java)
                        Triple(est, sub.getDouble(1), sub.getInt(2))
                    }
                }
                val prefsResponse = api.getEstablecimientosPersonalizados()
                if (prefsResponse.isSuccessful && prefsResponse.body() != null) {
                    val raw = prefsResponse.body()!!.string()
                    val arr = JSONArray(raw)
                    paraTi = (0 until arr.length()).map {
                        val sub = arr.getJSONArray(it)
                        val est = gson.fromJson(sub.getJSONObject(0).toString(), Establecimiento::class.java)
                        Triple(est, sub.getDouble(1), sub.getInt(2))
                    }
                }
            } catch (e: Exception) {
                Log.e("API", "Error: ${e.message}", e)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        CircleBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Mejores Establecimientos") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mejores) { item ->
                            FeaturedEstablecimientoCard(
                                establecimiento = item.first,
                                rating = item.second,
                                reviews = item.third,
                                onClick = {
                                    val id = item.first.id
                                    if (!id.isNullOrBlank()) navController.navigate("inicio_usuario_establecimiento/$id")
                                }
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 1, 2, 3).forEach { index ->
                            FilterChip(
                                selected = selectedChip == index,
                                onClick = { selectedChip = index },
                                label = { Text(listOf("Para ti", "Recientes", "Más visitados", "Low cost")[index]) },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            )
                        }
                    }
                }
                items(paraTi) { item ->
                    ForYouEstablecimientoCard(
                        establecimiento = item.first,
                        rating = item.second,
                        reviews = item.third,
                        onClick = {
                            val id = item.first.id
                            if (!id.isNullOrBlank()) navController.navigate("inicio_usuario_establecimiento/$id")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedEstablecimientoCard(
    establecimiento: Establecimiento,
    rating: Double,
    reviews: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val ctx = LocalContext.current
    val imgUrl = buildImageUrl(establecimiento.imagen_url)
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(150.dp)
            .clip(shape)
            .clickable { onClick() }
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
                        0.5f to Color.Transparent,
                        1f to Color(0xAA000000)
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(4.dp))
            Text("4 min", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                establecimiento.nombre_establecimiento,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${formatRating(rating)} (${formatReviews(reviews)})", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(shortAddress(establecimiento), color = Color.White, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(2.dp))
            Text(establecimiento.ambiente.joinToString(", "), color = Color.White, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ForYouEstablecimientoCard(
    establecimiento: Establecimiento,
    rating: Double,
    reviews: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val ctx = LocalContext.current
    val imgUrl = buildImageUrl(establecimiento.imagen_url)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .clickable { onClick() },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0x22000000))
    ) {
        Row(Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(imgUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .clip(shape),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxSize()
            ) {
                Text(
                    establecimiento.nombre_establecimiento,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    establecimiento.ambiente.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(shortAddress(establecimiento), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${formatRating(rating)} (${formatReviews(reviews)})", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default_establecimiento.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize(BASE_URL.trimEnd('/') + path)
}
private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
private fun shortAddress(e: Establecimiento): String = "C. ${e.nombre_establecimiento.take(12)}, 1"
private fun formatRating(r: Double): String { val x = (round(r * 10) / 10.0); return if (x % 1.0 == 0.0) "${x.toInt()}" else "$x" }
private fun formatReviews(n: Int): String = if (n >= 200) "200+" else "$n"
private const val BASE_URL = "http://127.0.0.1:5000"
