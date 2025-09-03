package com.example.hangout.ui.screens.admin

import android.net.Uri
import android.widget.Toast
import java.text.Normalizer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hangout.data.AmbientesProvider
import com.example.hangout.models.Establecimiento
import com.example.hangout.models.Evento
import com.example.hangout.models.Oferta
import com.example.hangout.network.RetrofitInstance
import com.example.hangout.ui.components.EventoCard
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatosEstablecimientoScreen(navController: NavController, data: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val refreshFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_establecimiento", false)
    val needRefresh by (refreshFlow?.collectAsState() ?: remember { mutableStateOf(false) })

    var obj by remember(data) { mutableStateOf<JSONObject?>(null) }
    val raw = remember(data) { Uri.decode(data).trim() }
    val isJson = remember(data) { raw.startsWith("{") && raw.endsWith("}") }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        if (isJson) {
            obj = JSONObject(raw)
        } else if (raw.isNotBlank()) {
            val api = RetrofitInstance.create(context)
            val resp = api.getEstablecimientoById(raw)
            if (resp.isSuccessful && resp.body() != null) {
                obj = JSONObject(estToJson(resp.body()!!))
            }
        }
    }

    LaunchedEffect(needRefresh, obj) {
        if (needRefresh == true) {
            val api = RetrofitInstance.create(context)
            val idToFetch = if (isJson) {
                obj?.optJSONObject("_id")?.optString("\$oid").orEmpty()
            } else {
                raw
            }
            if (idToFetch.isNotBlank()) {
                val resp = api.getEstablecimientoById(idToFetch)
                if (resp.isSuccessful && resp.body() != null) {
                    obj = JSONObject(estToJson(resp.body()!!))
                    Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                }
            }
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh_establecimiento", false)
        }
    }

    if (obj == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle") },
                    navigationIcon = {
                        Row {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Cargando…")
            }
        }

        if (showDeleteConfirm) {
            DeleteDialog(
                onCancel = { showDeleteConfirm = false },
                onConfirm = {
                    showDeleteConfirm = false
                }
            )
        }
        return
    }

    val decoded = obj!!
    val nombre = decoded.optString("nombre_establecimiento", "")
    val imagenHeader = buildImageUrl(decoded.optString("imagen_url", "/_uploads/photos/default_establecimiento.png"))
    val ambientes = decoded.optJSONArray("ambiente")?.toStringList() ?: emptyList()
    val eventosIds = decoded.optJSONArray("eventos")?.toStringList() ?: emptyList()
    val ofertasIds = decoded.optJSONArray("ofertas")?.toStringList() ?: emptyList()
    val rating = decoded.optDouble("rating", 0.0)
    val numeroReviews = decoded.optInt("numero_reviews", 0)
    val reviewsJson = decoded.optJSONArray("reviews") ?: JSONArray()
    val estId = decoded.optJSONObject("_id")?.optString("\$oid").orEmpty()
    val ambLookup = remember { buildAmbienteLookup() }

    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var ofertas by remember { mutableStateOf<List<Oferta>>(emptyList()) }

    LaunchedEffect(eventosIds.joinToString(",")) {
        val api = RetrofitInstance.create(context)
        val jobs = eventosIds.map { id -> async { api.getEventoById(id) } }
        val resps = jobs.awaitAll()
        eventos = resps.mapNotNull { it.body() }
    }

    LaunchedEffect(ofertasIds.joinToString(",")) {
        val api = RetrofitInstance.create(context)
        val jobs = ofertasIds.map { id -> async { api.getOfertaById(id) } }
        val resps = jobs.awaitAll()
        ofertas = resps.mapNotNull { it.body() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombre) },
                navigationIcon = {
                    Row {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("editar_establecimiento/$estId") }) { Icon(Icons.Filled.Edit, contentDescription = null) }
                    IconButton(onClick = { }) { Icon(Icons.Filled.Share, contentDescription = null) }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(imagenHeader).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
                Text(nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${formatRating(rating)} ★", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                    Text("$numeroReviews reviews", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(16.dp))
                Text("Ambiente:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ambientes) { a ->
                        val key = normalizeText(a)
                        val info = ambLookup[key]
                        AssistChip(
                            onClick = {},
                            label = { Text(info?.first ?: prettyCap(a)) },
                            leadingIcon = {
                                val resId = info?.second
                                if (resId != null) Icon(painterResource(id = resId), contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors()
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            item {
                SectionHeaderWithAdd(title = "Ofertas") { navController.navigate("crear_oferta/$estId") }
                Spacer(Modifier.height(8.dp))
            }
            item {
                if (ofertas.isEmpty()) {
                    Text("Sin ofertas", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(ofertas) { idx, o ->
                            OfertaCardSmall(oferta = o) {
                                val ofertaId = ofertasIds.getOrNull(idx) ?: return@OfertaCardSmall
                                navController.navigate("datos_oferta_admin/$ofertaId")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            item {
                SectionHeaderWithAdd(title = "Eventos") { navController.navigate("crear_evento/$estId") }
                Spacer(Modifier.height(8.dp))
            }
            item {
                if (eventos.isEmpty()) {
                    Text("Sin eventos", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(eventos) { idx, e ->
                            EventoCard(evento = e, onClick = {
                                val eventoId = eventosIds.getOrNull(idx) ?: return@EventoCard
                                navController.navigate("datos_evento_admin/$eventoId")
                            })
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            item {
                Text("Review:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
            }
            itemsIndexed(List(reviewsJson.length()) { it }) { index, i ->
                val r = reviewsJson.optJSONObject(i) ?: JSONObject()
                ReviewItemCard(
                    nombre = r.optString("nombre_usuario", "Usuario"),
                    calificacion = r.optDouble("calificacion", 0.0),
                    mensaje = r.optString("mensaje", ""),
                    avatarUrl = buildImageUrl(r.optString("imagen_url", "/_uploads/photos/default.png"))
                )
                if (index < reviewsJson.length() - 1) {
                    Divider(
                        thickness = 0.6.dp,
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showDeleteConfirm) {
        DeleteDialog(
            onCancel = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                if (estId.isBlank()) return@DeleteDialog
                scope.launch {
                    try {
                        val api = RetrofitInstance.create(context)
                        val resp = api.deleteEstablecimiento(estId)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Establecimiento eliminado", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "No se pudo eliminar", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun DeleteDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Eliminar establecimiento") },
        text = { Text("¿Seguro que deseas eliminar este establecimiento? Esta acción no se puede deshacer.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@Composable
private fun SectionHeaderWithAdd(title: String, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(6.dp))
        FilledTonalIconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun OfertaCardSmall(oferta: Oferta, onClick: () -> Unit) {
    val ctx = LocalContext.current
    val img = buildImageUrl(oferta.imagen_url ?: "/_uploads/photos/default_establecimiento.png")
    Card(
        modifier = Modifier
            .width(180.dp)
            .heightIn(min = 140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(img).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = oferta.nombre_oferta ?: "",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ReviewItemCard(nombre: String, calificacion: Double, mensaje: String, avatarUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingStars(calificacion)
                Spacer(Modifier.width(6.dp))
                Text(nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(4.dp))
            Text(mensaje, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RatingStars(value: Double) {
    val full = floor(value).toInt()
    val rest = 5 - full
    Row {
        repeat(full) { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
        repeat(rest) { Icon(Icons.Filled.StarBorder, contentDescription = null, modifier = Modifier.size(16.dp)) }
    }
}

private fun JSONArray.toStringList(): List<String> = List(length()) { i -> getString(i) }

/*
private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("http://127.0.0.1:5000/_uploads/photos/default.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize("http://127.0.0.1:5000$path")
}*/

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return "http://127.0.0.1:5000/_uploads/photos/default.png"
    if (path.startsWith("http")) return path
    return "http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}"
}


private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")

private fun formatRating(r: Double): String {
    val x = kotlin.math.round(r * 10) / 10.0
    return if (x % 1.0 == 0.0) "${x.toInt()}" else "$x"
}

private fun prettyCap(s: String): String = s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

private fun normalizeText(s: String): String {
    val n = Normalizer.normalize(s.lowercase().trim(), Normalizer.Form.NFD)
    return n.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}

private fun buildAmbienteLookup(): Map<String, Pair<String, Int>> {
    val base = mutableMapOf<String, Pair<String, Int>>()
    AmbientesProvider.listaAmbientes.forEach { amb ->
        val nombre = amb.name
        val icon = amb.imageRes
        val key = normalizeText(nombre)
        base[key] = nombre to icon
    }
    return base
}

private fun estToJson(est: Establecimiento): String {
    val root = JSONObject()
    val idObj = JSONObject().put("\$oid", est.id)
    root.put("_id", idObj)
    root.put("cif", est.cif)
    root.put("nombre_establecimiento", est.nombre_establecimiento)
    root.put("ambiente", JSONArray(est.ambiente))
    root.put("ofertas", JSONArray(est.ofertas))
    root.put("eventos", JSONArray(est.eventos))
    root.put("imagen_url", est.imagen_url)
    root.put("rating", 0.0)
    root.put("numero_reviews", est.reviews.size)
    root.put("reviews", JSONArray())
    return root.toString()
}
