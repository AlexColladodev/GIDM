package com.example.hangout.ui.screens.usuario

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.hangout.models.ActividadItem
import com.example.hangout.models.ParticipanteItem
import com.example.hangout.ui.components.ActividadListCard
import org.json.JSONArray
import com.example.hangout.ui.models.EventoUI
import com.example.hangout.ui.models.ActividadUI
import com.example.hangout.ui.models.ParticipanteUI

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EventosActividadesScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)

    var eventos by remember { mutableStateOf<List<EventoItem>>(emptyList()) }
    var actividades by remember { mutableStateOf<List<ActividadItem>>(emptyList()) }

    fun reloadActividades() {
        scope.launch {
            try {
                val api = RetrofitInstance.create(context)
                val respA = api.getMisActividadesRaw()
                if (!respA.isSuccessful) return@launch
                val rawA = respA.body()?.string().orEmpty()
                if (rawA.isBlank()) return@launch
                val arr = JSONArray(rawA)
                val list = mutableListOf<ActividadItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val id = o.optJSONObject("_id")?.optString("\$oid").orEmpty()
                    val nombre = o.optString("nombre_actividad", "")
                    val descripcion = o.optString("descripcion_actividad", "")
                    val fechaIso = o.optJSONObject("fecha_actividad")?.optString("\$date").orEmpty()
                    val hora = o.optString("hora_actividad", "")
                    val ubicacion = o.optString("ubicacion", "")
                    val idCreador = o.optString("id_usuario_creador", "")
                    val pArr = o.optJSONArray("perfil_participantes") ?: JSONArray()
                    val participantes = mutableListOf<ParticipanteItem>()
                    for (j in 0 until pArr.length()) {
                        val pj = pArr.getJSONObject(j)
                        participantes.add(
                            ParticipanteItem(
                                id = pj.optString("id", ""),
                                nombreUsuario = pj.optString("nombre_usuario", ""),
                                imagenUrl = pj.optString("imagen_url", "")
                            )
                        )
                    }
                    list.add(
                        ActividadItem(
                            id = id,
                            nombre = nombre,
                            descripcion = descripcion,
                            fechaIso = fechaIso,
                            hora = hora,
                            ubicacion = ubicacion,
                            idCreador = idCreador,
                            perfilParticipantes = participantes
                        )
                    )
                }
                actividades = list
            } catch (e: Exception) {
                Log.e("Actividades", "Error: ${e.message}", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val api = RetrofitInstance.create(context)
                val resp = api.getEventosOrdenadosRaw()
                if (!resp.isSuccessful) return@launch
                val raw = resp.body()?.string().orEmpty()
                if (raw.isBlank()) return@launch
                val json = JSONObject(raw)
                val arr = json.optJSONArray("eventos_ordenados") ?: return@launch
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
            } catch (e: Exception) {
                Log.e("Eventos", "Error: ${e.message}", e)
            }
        }
        reloadActividades()
    }

    val reloadSignal = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("reload_actividades", false)
        ?.collectAsState(initial = false)

    LaunchedEffect(reloadSignal?.value) {
        if (reloadSignal?.value == true) {
            reloadActividades()
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("reload_actividades", false)
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
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    val scopeLocal = rememberCoroutineScope()
                    val pager = pagerState
                    TabRow(
                        selectedTabIndex = pager.currentPage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = pager.currentPage == 0,
                            onClick = { scopeLocal.launch { pager.animateScrollToPage(0) } },
                            text = { Text("Eventos") }
                        )
                        Tab(
                            selected = pager.currentPage == 1,
                            onClick = { scopeLocal.launch { pager.animateScrollToPage(1) } },
                            text = { Text("Actividades") }
                        )
                    }
                    HorizontalPager(
                        state = pager,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> EventosPage(
                                eventos = eventos,
                                onClick = { e ->
                                    val gson = com.google.gson.Gson()
                                    val jsonSel = gson.toJson(
                                        EventoUI(
                                            id = e.id,
                                            nombre = e.nombre,
                                            descripcion = e.descripcion,
                                            fechaIso = e.fechaIso,
                                            precio = e.precio,
                                            hora = e.hora,
                                            idEstablecimiento = e.idEstablecimiento,
                                            nombreEstablecimiento = e.nombreEstablecimiento,
                                            imagenUrl = e.imagenUrl
                                        )
                                    )
                                    navController.currentBackStackEntry?.savedStateHandle?.set("evento_json", jsonSel)
                                    navController.navigate("datos_evento2")
                                }
                            )
                            1 -> ActividadesPage(
                                actividades = actividades,
                                onClick = { a ->
                                    val gson = com.google.gson.Gson()
                                    val jsonSel = gson.toJson(
                                        ActividadUI(
                                            id = a.id,
                                            nombre = a.nombre,
                                            descripcion = a.descripcion,
                                            fechaIso = a.fechaIso,
                                            hora = a.hora,
                                            ubicacion = a.ubicacion,
                                            idCreador = a.idCreador,
                                            perfil_participantes = a.perfilParticipantes.map {
                                                ParticipanteUI(
                                                    id = it.id,
                                                    nombre_usuario = it.nombreUsuario,
                                                    imagen_url = it.imagenUrl
                                                )
                                            }
                                        )
                                    )
                                    navController.currentBackStackEntry?.savedStateHandle?.set("actividad_json", jsonSel)
                                    navController.navigate("datos_actividad")
                                }
                            )
                        }
                    }
                }

                if (pagerState.currentPage == 1) {
                    FloatingActionButton(
                        onClick = { navController.navigate("crear_actividad") },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Crear actividad")
                    }
                }
            }
        }
    }
}

@Composable
private fun EventosPage(
    eventos: List<EventoItem>,
    onClick: (EventoItem) -> Unit
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
                onClick = { onClick(e) }
            )
        }
    }
}

@Composable
private fun ActividadesPage(
    actividades: List<ActividadItem>,
    onClick: (ActividadItem) -> Unit
) {
    if (actividades.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes actividades todavía")
        }
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(actividades, key = { it.id }) { a ->
            ActividadListCard(
                title = a.nombre,
                description = a.descripcion,
                dateText = formatDate(a.fechaIso),
                location = a.ubicacion,
                onClick = { onClick(a) }
            )
        }
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
        val outFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        outFmt.format(date!!)
    } catch (_: Exception) {
        iso
    }
}
/*
private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return emulatorize("$BASE_URL/_uploads/photos/default_establecimiento.png")
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
