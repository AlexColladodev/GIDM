package com.example.hangout.ui.screens.usuario

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hangout.models.UsuarioPerfil
import com.example.hangout.models.ParticipantePerfil
import com.example.hangout.models.ActividadPerfil
import com.example.hangout.models.ReviewPerfil
import com.example.hangout.network.RetrofitInstance
import com.example.hangout.ui.components.ActividadMiniCard
import com.example.hangout.ui.components.CircleBackground
import com.example.hangout.ui.components.ReviewMiniCard
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.google.gson.Gson

data class PerfilData(
    val usuario: UsuarioPerfil,
    val actividades: List<ActividadPerfil>,
    val reviews: List<ReviewPerfil>
)

data class PerfilSeed(
    val nombre: String,
    val nombre_usuario: String,
    val email: String,
    val telefono: String,
    val preferencias: List<String>,
    val imagen_url: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DatosPerfilScreen(navController: NavController) {
    val context = LocalContext.current

    var perfil by remember { mutableStateOf<PerfilData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun loadPerfil() {
        try {
            error = null
            val api = RetrofitInstance.create(context)
            val resp: Response<ResponseBody> = api.getDatosUsuarioRaw()
            if (!resp.isSuccessful) {
                error = "HTTP ${resp.code()}"
            } else {
                val raw = resp.body()?.string().orEmpty()
                perfil = parsePerfil(raw)
            }
        } catch (e: Exception) {
            Log.e("Perfil", "Error: ${e.message}", e)
            error = e.message ?: "Error desconocido"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        loadPerfil()
    }

    val reloadSignal = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("reload_perfil", false)
        ?.collectAsState(initial = false)

    LaunchedEffect(reloadSignal?.value) {
        if (reloadSignal?.value == true) {
            loading = true
            loadPerfil()
            navController.currentBackStackEntry?.savedStateHandle?.set("reload_perfil", false)
        }
    }

    Box(Modifier.fillMaxSize()) {
        CircleBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Perfil") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            perfil?.let { p ->
                                val seed = PerfilSeed(
                                    nombre = p.usuario.nombre,
                                    nombre_usuario = p.usuario.nombreUsuario,
                                    email = p.usuario.email,
                                    telefono = p.usuario.telefono,
                                    preferencias = p.usuario.preferencias,
                                    imagen_url = p.usuario.imagenUrl
                                )
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("perfil_seed_json", Gson().toJson(seed))
                            }
                            navController.navigate("editar_perfil")
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                        IconButton(onClick = {
                            val text = "Sígueme en HangOut: ${perfil?.usuario?.nombreUsuario ?: ""}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir perfil"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            when {
                loading -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text("Error: $error") }

                perfil == null -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text("Sin datos") }

                else -> {
                    val p = perfil!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = buildImageUrl(p.usuario.imagenUrl),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(116.dp)
                                        .clip(CircleShape)
                                )
                                Text(
                                    p.usuario.nombre,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ReadOnlyField("Nombre de usuario", p.usuario.nombreUsuario)
                                ReadOnlyField("Fecha de nacimiento", formatDateOnly(p.usuario.fechaNacIso))
                                ReadOnlyField("Correo electrónico", p.usuario.email)
                                ReadOnlyField("Teléfono", p.usuario.telefono)
                            }
                        }

                        item { PreferencesSection(p.usuario.preferencias) }

                        item {
                            Text(
                                "Actividades creadas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            if (p.actividades.isEmpty()) {
                                EmptyBox("Aún no hay actividades")
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(p.actividades, key = { it.id }) { a ->
                                        ActividadMiniCard(
                                            title = a.nombre,
                                            subtitle = a.ubicacion,
                                            dateText = formatDateTime(a.fechaIso)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                "Mis reviews",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            if (p.reviews.isEmpty()) {
                                EmptyBox("Aún no has publicado reviews")
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(p.reviews, key = { it.id }) { r ->
                                        ReviewMiniCard(
                                            establecimiento = r.nombreEstablecimiento,
                                            rating = r.calificacion,
                                            mensaje = r.mensaje,
                                            fecha = formatDateTime(r.fechaCreacionIso)
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferencesSection(preferencias: List<String>) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Preferencias",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (preferencias.isEmpty()) {
            EmptyBox("Sin preferencias")
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                preferencias.forEach { raw ->
                    val iconName = raw.lowercase(Locale.getDefault())
                    val iconRes = remember(iconName) {
                        context.resources.getIdentifier(iconName, "drawable", context.packageName)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (iconRes != 0) {
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(raw, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

private fun parsePerfil(raw: String): PerfilData {
    val root = JSONObject(raw)

    val u = root.getJSONObject("usuario")
    val usuario = UsuarioPerfil(
        id = u.optJSONObject("_id")?.optString("\$oid").orEmpty(),
        nombre = u.optString("nombre", ""),
        nombreUsuario = u.optString("nombre_usuario", ""),
        email = u.optString("email", ""),
        telefono = u.optString("telefono", ""),
        fechaNacIso = u.optJSONObject("fecha_nac")?.optString("\$date").orEmpty(),
        imagenUrl = u.optString("imagen_url", ""),
        preferencias = u.optJSONArray("preferencias")?.toListString() ?: emptyList()
    )

    val actividadesArr = root.optJSONArray("actividades") ?: JSONArray()
    val actividades = mutableListOf<ActividadPerfil>()
    for (i in 0 until actividadesArr.length()) {
        val o = actividadesArr.getJSONObject(i)
        val perfilArr = o.optJSONArray("perfil_participantes") ?: JSONArray()
        val participantes = mutableListOf<ParticipantePerfil>()
        for (j in 0 until perfilArr.length()) {
            val pj = perfilArr.getJSONObject(j)
            participantes.add(
                ParticipantePerfil(
                    nombreUsuario = pj.optString("nombre_usuario", ""),
                    imagenUrl = pj.optString("imagen_url", "")
                )
            )
        }
        actividades.add(
            ActividadPerfil(
                id = o.optJSONObject("_id")?.optString("\$oid").orEmpty(),
                nombre = o.optString("nombre_actividad", ""),
                descripcion = o.optString("descripcion_actividad", ""),
                fechaIso = o.optJSONObject("fecha_actividad")?.optString("\$date").orEmpty(),
                hora = o.optString("hora_actividad", ""),
                ubicacion = o.optString("ubicacion", ""),
                participantes = participantes
            )
        )
    }

    val reviewsArr = root.optJSONArray("reviews") ?: JSONArray()
    val reviews = mutableListOf<ReviewPerfil>()
    for (i in 0 until reviewsArr.length()) {
        val r = reviewsArr.getJSONObject(i)
        reviews.add(
            ReviewPerfil(
                id = r.optJSONObject("_id")?.optString("\$oid").orEmpty(),
                calificacion = r.optDouble("calificacion", 0.0),
                mensaje = r.optString("mensaje", ""),
                nombreEstablecimiento = r.optString("nombre_establecimiento", ""),
                fechaCreacionIso = r.optString("fecha_creacion", "")
            )
        )
    }

    return PerfilData(usuario = usuario, actividades = actividades, reviews = reviews)
}

private fun JSONArray.toListString(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until length()) list.add(optString(i, ""))
    return list
}

private fun formatDateOnly(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inFmt.timeZone = TimeZone.getTimeZone("UTC")
        val date = inFmt.parse(iso)
        val outFmt = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es"))
        outFmt.format(date!!)
    } catch (_: Exception) {
        iso
    }
}

private fun formatDateTime(iso: String): String {
    if (iso.isBlank()) return ""
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inFmt.parse(iso)
        val outFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        outFmt.format(date!!)
    } catch (_: Exception) {
        try {
            val inFmt2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inFmt2.timeZone = TimeZone.getTimeZone("UTC")
            val date2 = inFmt2.parse(iso)
            val outFmt2 = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            outFmt2.format(date2!!)
        } catch (_: Exception) {
            iso
        }
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
