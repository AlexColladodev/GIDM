package com.example.hangout.ui.screens.usuario

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hangout.models.Establecimiento
import com.example.hangout.network.RetrofitInstance
import com.example.hangout.ui.components.CircleBackground
import com.example.hangout.ui.components.EventListCard
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import okhttp3.ResponseBody


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventosActividadesScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)


    var eventos by remember { mutableStateOf<List<EventoItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val api = RetrofitInstance.create(context)

                val resp = api.getEventosOrdenadosRaw()
                if (!resp.isSuccessful) {
                    Log.e("Eventos", "HTTP ${resp.code()}: ${resp.errorBody()?.string()}")
                    return@launch
                }
                val raw = resp.body()?.string().orEmpty()
                if (raw.isBlank()) {
                    Log.e("Eventos", "Respuesta vacía")
                    return@launch
                }

                val json = JSONObject(raw)
                val arr = json.optJSONArray("eventos_ordenados") ?: run {
                    Log.e("Eventos", "No viene 'eventos_ordenados' en el JSON")
                    return@launch
                }

                val tmp = mutableListOf<EventoItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val id = o.optJSONObject("_id")?.optString("\$oid").orEmpty()
                    val nombre = o.optString("nombre_evento", "")
                    val descripcion = o.optString("descripcion_evento", "")
                    val fechaIso = o.optJSONObject("fecha_evento")?.optString("\$date").orEmpty()
                    val precio = o.optDouble("precio", 0.0)
                    val hora = o.optString("hora_evento", "")
                    val establecimientoId = o.optString("id_establecimiento", "")
                    val imagen = o.optString("imagen_url", "")

                    var nombreEst = ""
                    try {
                        if (establecimientoId.isNotBlank()) {
                            val rEst = api.getEstablecimientoById(establecimientoId)
                            if (rEst.isSuccessful) {
                                val est: Establecimiento? = rEst.body()
                                nombreEst = est?.nombre_establecimiento.orEmpty()
                            }
                        }
                    } catch (_: Exception) {}

                    tmp.add(
                        EventoItem(
                            id = id,
                            nombre = nombre,
                            descripcion = descripcion,
                            fechaIso = fechaIso,
                            precio = precio,
                            hora = hora,
                            idEstablecimiento = establecimientoId,
                            nombreEstablecimiento = nombreEst,
                            imagenUrl = imagen
                        )
                    )
                }
                eventos = tmp
                Log.d("Eventos", "Cargados ${eventos.size} eventos")
            } catch (e: Exception) {
                Log.e("Eventos", "Error cargando eventos: ${e.message}", e)
            }
        }
    }


    Box(Modifier.fillMaxSize()) {
        CircleBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Event, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Eventos y Actividades")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("Eventos") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("Actividades") }
                    )
                }

                HorizontalPager(

                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> EventosPage(
                            eventos = eventos,
                            onClick = { id ->
                                if (id.isNotBlank()) navController.navigate("datos_evento")
                            }
                        )
                        1 -> ActividadesPlaceholderPage()
                    }
                }
            }
        }
    }
}

@Composable
private fun EventosPage(
    eventos: List<EventoItem>,
    onClick: (String) -> Unit
) {
    if (eventos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay eventos disponibles")
        }
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(eventos, key = { it.id }) { e ->
            EventListCard(
                title = e.nombre,
                subtitle = e.nombreEstablecimiento.ifBlank { "Establecimiento" },
                address = "",
                dateText = formatDate(e.fechaIso),
                imageUrl = buildImageUrl(e.imagenUrl),
                onClick = { onClick(e.id) }
            )
        }
    }
}

@Composable
private fun ActividadesPlaceholderPage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pronto verás tus actividades aquí")
    }
}

private data class EventoItem(
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

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default_establecimiento.png")
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize(BASE_URL.trimEnd('/') + path)
}
private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
private const val BASE_URL = "http://127.0.0.1:5000"
