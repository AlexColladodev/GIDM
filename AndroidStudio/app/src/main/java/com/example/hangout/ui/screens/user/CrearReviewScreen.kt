package com.example.hangout.ui.screens.usuario

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hangout.models.Review
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearReviewScreen(
    navController: NavController,
    establecimientoId: String,
    initialRating: Int
) {
    val context = LocalContext.current
    var rating by remember { mutableStateOf(initialRating.coerceIn(1, 5)) }
    var mensaje by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Leer userId desde el JWT guardado en SharedPreferences("auth") -> "token" (ver LoginScreen)
    LaunchedEffect(Unit) {
        userId = obtainUserId(context)
        if (userId.isNullOrBlank()) {
            error = "No se pudo obtener el id del usuario. Inicia sesión de nuevo."
        }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Crear reseña") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Calificación", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { i ->
                    val filled = i <= rating
                    Icon(
                        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(34.dp)
                            .padding(end = 6.dp)
                            .clickable { rating = i }
                    )
                }
            }

            Text("Tu reseña", style = MaterialTheme.typography.titleMedium)
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                BasicTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .padding(12.dp),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            if (error != null) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (loading) return@Button
                    if (userId.isNullOrBlank()) {
                        error = "No se pudo identificar al usuario."
                        return@Button
                    }
                    loading = true
                    error = null
                    scope.launch {
                        try {
                            val api = RetrofitInstance.create(context)
                            val nowIso = nowIsoUtc()

                            // El backend sobreescribe id_usuario con el JWT, pero lo enviamos igual sin problema
                            val body = Review(
                                _id = null,
                                calificacion = rating.toFloat(),
                                mensaje = mensaje,
                                id_usuario = userId!!,
                                id_establecimiento = establecimientoId,
                                fecha_creacion = nowIso
                            )

                            val resp = api.createReviewGateway(body)
                            if (resp.isSuccessful) {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("review_creada", true)
                                navController.popBackStack()
                            } else {
                                error = "Error ${resp.code()}"
                            }
                        } catch (e: Exception) {
                            Log.e("CrearReview", "error", e)
                            error = e.message ?: "Error desconocido"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading && mensaje.isNotBlank() && !userId.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Publicar review")
            }
        }
    }
}

/* -------------------- Helpers -------------------- */

// Lee el token de SharedPreferences("auth","token") y saca el id del payload (sub/identity/user_id/id/_id)
private fun obtainUserId(context: Context): String? {
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val token = prefs.getString("token", null) // guardado en LoginScreen
    if (token.isNullOrBlank()) return null
    return decodeJwtForUserId(token)
}

private fun decodeJwtForUserId(jwt: String): String? {
    val parts = jwt.split(".")
    if (parts.size < 2) return null
    return try {
        val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
        val json = JSONObject(String(payloadBytes, Charset.forName("UTF-8")))
        when {
            json.has("sub") -> json.optString("sub", null)
            json.has("identity") -> json.optString("identity", null)
            json.has("user_id") -> json.optString("user_id", null)
            json.has("id") -> json.optString("id", null)
            json.has("_id") -> {
                val v = json.get("_id")
                if (v is JSONObject) v.optString("\$oid", null) else v.toString()
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

// Fecha actual en ISO-8601 UTC con 'Z'
private fun nowIsoUtc(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    fmt.timeZone = TimeZone.getTimeZone("UTC")
    return fmt.format(Date())
}
