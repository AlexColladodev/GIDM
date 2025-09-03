package com.example.hangout.ui.screens.admin

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioAdministradorScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var establecimientos by remember { mutableStateOf<List<AdminEstItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val api = RetrofitInstance.create(context)
                val resp = api.getMiPerfilAdmin()
                if (resp.isSuccessful && resp.body() != null) {
                    establecimientos = parseAdminPerfil(resp.body()!!)
                } else {
                    Log.e("AdminPerfil", "HTTP ${resp.code()}")
                }
            } catch (e: Exception) {
                Log.e("AdminPerfil", "Error ${e.message}", e)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        CircleBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Inicio Administrador") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("crear_establecimiento") }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(establecimientos) { item ->
                    AdminEstablecimientoCard(item) {
                        val encoded = Uri.encode(item.rawJson.toString())
                        navController.navigate("admin_establecimiento_detalle?data=$encoded")
                    }
                }
            }
        }
    }
}

private data class AdminEstItem(
    val establecimiento: Establecimiento,
    val rating: Double,
    val numeroReviews: Int,
    val rawJson: JSONObject
)

private fun parseAdminPerfil(body: ResponseBody): List<AdminEstItem> {
    val txt = body.string()
    val root = JSONObject(txt)
    val arr = root.getJSONArray("establecimientos_detalle")
    val out = mutableListOf<AdminEstItem>()
    for (i in 0 until arr.length()) {
        val estJson = arr.getJSONObject(i)
        val est = jsonToEstablecimiento(estJson)
        val rating = if (estJson.has("rating")) estJson.optDouble("rating", 0.0) else 0.0
        val nReviews = if (estJson.has("numero_reviews")) estJson.optInt("numero_reviews", 0) else 0
        out += AdminEstItem(est, rating, nReviews, estJson)
    }
    return out
}

private fun jsonToEstablecimiento(obj: JSONObject): Establecimiento {
    val id = obj.getJSONObject("_id").getString("\$oid")
    val cif = obj.optString("cif", "")
    val nombre = obj.optString("nombre_establecimiento", "")
    val ambiente = obj.optJSONArray("ambiente")?.let { jarr ->
        List(jarr.length()) { idx -> jarr.getString(idx) }
    } ?: emptyList()
    val ofertas = toStringList(obj.optJSONArray("ofertas"))
    val eventos = toStringList(obj.optJSONArray("eventos"))
    val reviews = extractReviewIds(obj.optJSONArray("reviews"))
    val imagenUrl = obj.optString("imagen_url", "/_uploads/photos/default_establecimiento.png")
    return Establecimiento(
        _id = com.example.hangout.models.IdWrapper(id),
        cif = cif,
        nombre_establecimiento = nombre,
        id_administrador = "",
        ambiente = ambiente,
        ofertas = ofertas,
        eventos = eventos,
        reviews = reviews,
        imagen_url = imagenUrl
    )
}

private fun toStringList(arr: JSONArray?): List<String> {
    if (arr == null) return emptyList()
    return List(arr.length()) { i -> arr.getString(i) }
}

private fun extractReviewIds(arr: JSONArray?): List<String> {
    if (arr == null) return emptyList()
    val list = mutableListOf<String>()
    for (i in 0 until arr.length()) {
        val r = arr.getJSONObject(i)
        val id = r.optJSONObject("_id")?.optString("\$oid") ?: ""
        if (id.isNotBlank()) list += id
    }
    return list
}

@Composable
private fun AdminEstablecimientoCard(item: AdminEstItem, onClick: () -> Unit) {
    val e = item.establecimiento
    val ctx = LocalContext.current
    val imgUrl = buildImageUrl(e.imagen_url)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0x22000000))
    ) {
        Row(Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(imgUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxSize()
            ) {
                Text(
                    e.nombre_establecimiento,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    e.ambiente.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "${formatRating(item.rating)} ★",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "${formatReviews(item.numeroReviews)} valoraciones",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
/*
private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default_establecimiento.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize(BASE_URL.trimEnd('/') + path)
}*/
private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return "http://127.0.0.1:5000/_uploads/photos/default.png"
    if (path.startsWith("http")) return path
    return "http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}"
}


private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
private fun formatRating(r: Double): String {
    val x = (kotlin.math.round(r * 10) / 10.0)
    return if (x % 1.0 == 0.0) "${x.toInt()}" else "$x"
}
private fun formatReviews(n: Int): String = if (n >= 200) "200+" else "$n"
private const val BASE_URL = "http://127.0.0.1:5000"
