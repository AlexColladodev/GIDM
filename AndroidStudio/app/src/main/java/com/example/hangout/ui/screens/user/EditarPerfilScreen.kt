package com.example.hangout.ui.screens.usuario

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.hangout.data.AmbientesProvider
import com.example.hangout.models.Ambiente
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.text.Normalizer
import java.util.Locale

data class UpdateUsuarioRequest(
    val nombre: String,
    val nombre_usuario: String,
    val email: String,
    val telefono: String,
    val preferencias: List<String>
)

private fun normalizeKey(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)

private fun parseCsrfFromCookie(cookie: String?): String? {
    if (cookie.isNullOrBlank()) return null
    val parts = cookie.split(';')
    for (p in parts) {
        val kv = p.trim().split('=', limit = 2)
        if (kv.size == 2 && kv[0] == "csrf_access_token") return kv[1]
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var nombre by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var selectedAmbientes by remember { mutableStateOf(setOf<Ambiente>()) }

    LaunchedEffect(Unit) {
        try {
            val api = RetrofitInstance.create(context)
            val resp = api.getDatosUsuarioRaw()
            if (!resp.isSuccessful) {
                error = "HTTP ${resp.code()}"
            } else {
                val raw = resp.body()?.string().orEmpty()
                val root = JSONObject(raw).getJSONObject("usuario")

                nombre = root.optString("nombre", "")
                nombreUsuario = root.optString("nombre_usuario", "")
                email = root.optString("email", "")
                telefono = root.optString("telefono", "")

                val prefsJson = root.optJSONArray("preferencias")
                val prefsBackend = mutableSetOf<String>()
                if (prefsJson != null) {
                    for (i in 0 until prefsJson.length()) {
                        prefsBackend += normalizeKey(prefsJson.optString(i))
                    }
                }
                val all = AmbientesProvider.listaAmbientes
                selectedAmbientes = all.filter { normalizeKey(it.name) in prefsBackend }.toSet()
            }
        } catch (e: Exception) {
            error = e.message ?: "Error al cargar perfil"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
                    )
                },
                snackbarHost = { SnackbarHost(snack) }
            ) { padding ->
                when {
                    loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }

                    error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) { Text("Error: $error") }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(padding)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = nombreUsuario,
                                onValueChange = { nombreUsuario = it },
                                label = { Text("Usuario") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Correo electrónico") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = telefono,
                                onValueChange = { telefono = it },
                                label = { Text("Teléfono") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text("Preferencias", style = MaterialTheme.typography.titleMedium)

                            val ambientes = AmbientesProvider.listaAmbientes
                            val filas = ambientes.chunked(4)

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                filas.forEach { fila ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        fila.forEach { ambiente ->
                                            val seleccionado = ambiente in selectedAmbientes
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .width(80.dp)
                                                    .clickable {
                                                        selectedAmbientes = if (seleccionado) {
                                                            selectedAmbientes - ambiente
                                                        } else {
                                                            selectedAmbientes + ambiente
                                                        }
                                                    }
                                                    .padding(4.dp)
                                            ) {
                                                Surface(
                                                    tonalElevation = if (seleccionado) 4.dp else 0.dp,
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = if (seleccionado)
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    else
                                                        Color.Transparent
                                                ) {
                                                    Image(
                                                        painter = rememberAsyncImagePainter(ambiente.imageRes),
                                                        contentDescription = ambiente.name,
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .padding(8.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = ambiente.name,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        if (nombre.isBlank() || nombreUsuario.isBlank() || email.isBlank()) {
                                            snack.showSnackbar("Completa los campos obligatorios")
                                            return@launch
                                        }

                                        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                        val token = prefs.getString("token", null)
                                        val cookie = prefs.getString("cookie", null)
                                        val csrf = parseCsrfFromCookie(cookie)

                                        if (token.isNullOrBlank() && cookie.isNullOrBlank()) {
                                            snack.showSnackbar("No hay credenciales (token/cookie) para autenticar.")
                                            return@launch
                                        }

                                        saving = true

                                        val prefsNormalized = selectedAmbientes.map { normalizeKey(it.name) }

                                        val json = JSONObject().apply {
                                            put("nombre", nombre.trim())
                                            put("nombre_usuario", nombreUsuario.trim())
                                            put("email", email.trim())
                                            put("telefono", telefono.trim())
                                            put("preferencias", JSONArray(prefsNormalized))
                                        }.toString()

                                        val BASE_URL = "http://127.0.0.1:5000/"
                                        val url = BASE_URL + "usuario_generico"

                                        val mediaType = "application/json; charset=utf-8".toMediaType()
                                        val requestBody = json.toRequestBody(mediaType)

                                        val builder = Request.Builder()
                                            .url(url)
                                            .put(requestBody)
                                            .header("Accept", "application/json")
                                            .header("Content-Type", "application/json")

                                        token?.let { builder.header("Authorization", "Bearer $it") }
                                        cookie?.let { builder.header("Cookie", it) }
                                        csrf?.let { builder.header("X-CSRF-TOKEN", it) }

                                        val client = OkHttpClient()
                                        val request = builder.build()

                                        val code = withContext(Dispatchers.IO) {
                                            client.newCall(request).execute().use { resp ->
                                                resp.code
                                            }
                                        }

                                        saving = false
                                        if (code in 200..299) {
                                            navController.navigate("datos_perfil") {
                                                popUpTo("datos_perfil") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            snack.showSnackbar("No se pudo actualizar ($code)")
                                        }
                                    }
                                },
                                enabled = !saving,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                if (saving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Guardar cambios")
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
