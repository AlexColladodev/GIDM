package com.example.hangout.ui.screens.usuario

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hangout.models.Establecimiento
import com.example.hangout.network.RetrofitInstance
import com.example.hangout.ui.components.EstablecimientoCard
import com.example.hangout.ui.components.CircleBackground
import kotlinx.coroutines.launch
import org.json.JSONArray
import com.google.gson.Gson
import androidx.compose.ui.graphics.Color
import com.example.hangout.models.Evento
import com.example.hangout.ui.components.EventoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioUsuarioScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var preferidos by remember { mutableStateOf<List<Establecimiento>>(emptyList()) }
    var mejores by remember { mutableStateOf<List<Establecimiento>>(emptyList()) }
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val api = RetrofitInstance.create(context)
                val gson = Gson()

                val prefsResponse = api.getEstablecimientosPersonalizados()
                val mejoresResponse = api.getEstablecimientosOrdenados()
                val eventosResponse = api.getEventosOrdenados()

                // --- ESTABLECIMIENTOS ---
                if (prefsResponse.isSuccessful && prefsResponse.body() != null) {
                    val raw = prefsResponse.body()!!.string()
                    val jsonArray = JSONArray(raw)
                    preferidos = (0 until jsonArray.length()).mapNotNull { i ->
                        val subArray = jsonArray.getJSONArray(i)
                        val estJson = subArray.getJSONObject(0)
                        gson.fromJson(estJson.toString(), Establecimiento::class.java)
                    }
                }

                if (mejoresResponse.isSuccessful && mejoresResponse.body() != null) {
                    val raw = mejoresResponse.body()!!.string()
                    val jsonArray = JSONArray(raw)
                    mejores = (0 until jsonArray.length()).mapNotNull { i ->
                        val subArray = jsonArray.getJSONArray(i)
                        val estJson = subArray.getJSONObject(0)
                        gson.fromJson(estJson.toString(), Establecimiento::class.java)
                    }
                }

                // --- EVENTOS ---
                if (eventosResponse.isSuccessful && eventosResponse.body() != null) {
                    val ids = eventosResponse.body()!!["eventos_ordenados"] ?: emptyList()
                    val eventosCargados = mutableListOf<Evento>()
                    for (id in ids) {
                        val eventoResp = api.getEventoById(id)
                        if (eventoResp.isSuccessful) {
                            eventoResp.body()?.let { eventosCargados.add(it) }
                        }
                    }
                    eventos = eventosCargados
                    Log.d("EVENTOS", "Eventos cargados: ${eventos.size}")
                }

            } catch (e: Exception) {
                Log.e("API", "Error al cargar datos: ${e.message}", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CircleBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Inicio") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = "Establecimientos basados en tus preferencias",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(preferidos) { establecimiento ->
                            EstablecimientoCard(establecimiento)
                        }
                    }
                }

                item {
                    Text(
                        text = "Mejores Establecimientos",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mejores) { establecimiento ->
                            EstablecimientoCard(establecimiento)
                        }
                    }
                }

                item {
                    Text(
                        text = "Eventos próximos",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(eventos) { evento ->
                            EventoCard(evento = evento, onClick = { /* navegación */ })
                        }
                    }
                }
            }
        }
    }
}
