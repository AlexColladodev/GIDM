package com.example.hangout.ui.screens.usuario

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hangout.models.Establecimiento
import com.example.hangout.models.EstablecimientoRating
import com.example.hangout.models.Evento
import com.example.hangout.models.Oferta
import com.example.hangout.models.ReviewResponse
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import kotlin.math.round
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.google.gson.Gson
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DatosEstablecimientoScreen(
    navController: NavController,
    establecimientoId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var est by remember { mutableStateOf<Establecimiento?>(null) }
    var rating by remember { mutableStateOf(0.0) }
    var reviewsCount by remember { mutableStateOf(0) }

    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var ofertas by remember { mutableStateOf<List<Oferta>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<ReviewResponse>>(emptyList()) }

    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(establecimientoId) {
        scope.launch {
            try {
                val api = RetrofitInstance.create(context)

                val rEst = api.getEstablecimientoById(establecimientoId)
                if (rEst.isSuccessful) {
                    est = rEst.body()
                }

                val rRat = api.getEstablecimientoRating(establecimientoId)
                if (rRat.isSuccessful) {
                    val rr: EstablecimientoRating? = rRat.body()
                    rating = rr?.media ?: 0.0
                    reviewsCount = rr?.n_reviews ?: 0
                }

                val evs = mutableListOf<Evento>()
                est?.eventos?.forEach { id ->
                    val r = api.getEventoById(id)
                    if (r.isSuccessful) r.body()?.let { evs.add(it) }
                }
                eventos = evs

                val ofs = mutableListOf<Oferta>()
                est?.ofertas?.forEach { id ->
                    val r = api.getOfertaById(id)
                    if (r.isSuccessful) r.body()?.let { ofs.add(it) }
                }
                ofertas = ofs

                val revs = mutableListOf<ReviewResponse>()
                est?.reviews?.forEach { id ->
                    val r = api.getReviewById(id)
                    if (r.isSuccessful) r.body()?.let { revs.add(it) }
                }
                reviews = revs
            } catch (e: Exception) {
                Log.e("API", "Error: ${e.message}", e)
            }
        }
    }

    val nombre = est?.nombre_establecimiento ?: ""
    val imgUrl = buildImageUrl(est?.imagen_url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (nombre.isBlank()) "Establecimiento" else nombre) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Filled.Share, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        val gson = remember { Gson() }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    val ctx = LocalContext.current
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
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = {}) { Icon(Icons.Filled.FavoriteBorder, contentDescription = null) }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        val txt = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Calle Ángel Ganivet, 12")
                            }
                        }
                        Text(txt)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${formatRating(rating)} (${formatReviews(reviewsCount)})")
                    }
                }

                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    est?.ambiente?.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tag.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                            leadingIcon = {
                                Box(
                                    Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x11000000))
                                )
                            }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = if (selectedTab == 0) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Ofertas")
                    }
                    OutlinedButton(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = if (selectedTab == 1) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Eventos")
                    }
                }
            }

            if (selectedTab == 0) {
                items(ofertas) { o ->
                    OfertaEventoCard(
                        titulo = o.nombre_oferta ?: "",
                        descripcion = o.descripcion_oferta ?: "",
                        precio = o.precio_oferta?.toString() ?: "",
                        imagen = buildImageUrl(o.imagen_url),
                        onClick = {
                            val json = gson.toJson(o)
                            navController.currentBackStackEntry?.savedStateHandle?.set("oferta_json", json)
                            navController.currentBackStackEntry?.savedStateHandle?.set("establecimiento_nombre_arg", est?.nombre_establecimiento ?: "Establecimiento")
                            navController.navigate("datos_oferta")
                        }
                    )
                }
            } else {
                items(eventos) { e ->
                    OfertaEventoCard(
                        titulo = e.nombre_evento ?: "",
                        descripcion = e.descripcion_evento ?: "",
                        precio = e.precio?.toString() ?: "",
                        imagen = buildImageUrl(e.imagen_url),
                        onClick = {
                            val json = gson.toJson(e)
                            navController.currentBackStackEntry?.savedStateHandle?.set("evento_json", json)
                            navController.currentBackStackEntry?.savedStateHandle?.set("establecimiento_nombre_arg", est?.nombre_establecimiento ?: "Establecimiento")
                            navController.navigate("datos_evento")
                        }
                    )
                }
            }

            item {
                Text("Reseñas", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text("Calificar y opinar:")
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(5) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0x22000000)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            items(reviews.take(1)) { r ->
                ReviewCard(
                    usuario = r.nombre_usuario ?: "Usuario",
                    calificacion = r.review?.calificacion ?: 0f,
                    mensaje = r.review?.mensaje ?: ""
                )
            }

            item {
                Row(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    Text("Ver todas", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OfertaEventoCard(titulo: String, descripcion: String, precio: String, imagen: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clickable { onClick() },
        shape = shape,
        border = BorderStroke(1.dp, Color(0x22000000))
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            val ctx = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(imagen).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(shape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (precio.isNotBlank()) Text("$precio €", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(2.dp))
                Text(descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}

@Composable
private fun ReviewCard(usuario: String, calificacion: Float, mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0x33000000))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) {
                        val filled = it < calificacion.toInt().coerceIn(0, 5)
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (filled) Color(0xFFFFC107) else Color(0x22000000),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(usuario, fontWeight = FontWeight.SemiBold)
            }
            Text(mensaje, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default_establecimiento.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize(BASE_URL.trimEnd('/') + path)
}
private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
private fun formatRating(r: Double): String {
    val x = (round(r * 10) / 10.0)
    return if (x % 1.0 == 0.0) "${x.toInt()}" else "$x"
}
private fun formatReviews(n: Int): String = if (n >= 200) "200+" else "$n"
private const val BASE_URL = "http://127.0.0.1:5000"
