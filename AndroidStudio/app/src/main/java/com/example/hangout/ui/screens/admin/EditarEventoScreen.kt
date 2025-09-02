package com.example.hangout.ui.screens.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hangout.models.Evento
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarEventoScreen(
    navController: NavController,
    id: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var evento by remember { mutableStateOf<Evento?>(null) }
    var loading by remember { mutableStateOf(true) }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    val cal = remember { Calendar.getInstance() }

    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            val mm = (m + 1).toString().padStart(2, '0')
            val dd = d.toString().padStart(2, '0')
            fecha = "$y-$mm-$dd"
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    val timePicker = TimePickerDialog(
        context,
        { _, h, min ->
            val hh = h.toString().padStart(2, '0')
            val mm = min.toString().padStart(2, '0')
            hora = "$hh:$mm:00"
        },
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE),
        true
    )

    LaunchedEffect(id) {
        try {
            val api = RetrofitInstance.create(context)
            val resp = api.getEventoById(id)
            val e = resp.body()
            evento = e
            if (e != null) {
                nombre = e.nombre_evento ?: ""
                descripcion = e.descripcion_evento ?: ""
                precioText = e.precio.toString()
                fecha = extractDateYMD(e.fecha_evento) ?: ""
                hora = ensureSeconds(e.hora_evento ?: "")
            }
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val base = evento ?: return@IconButton
                        val body = mutableMapOf<String, Any>()

                        val n = nombre.trim()
                        if (n.isNotEmpty() && n != base.nombre_evento) body["nombre_evento"] = n

                        val d = descripcion.trim()
                        if (d.isNotEmpty() && d != base.descripcion_evento) body["descripcion_evento"] = d

                        val p = precioText.replace(',', '.').toFloatOrNull()
                        if (p == null) {
                            Toast.makeText(context, "Precio inválido", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        } else if (p != base.precio) {
                            body["precio"] = p
                        }

                        val f = fecha.trim()
                        if (f.isNotEmpty()) {
                            if (!isYyyyMmDd(f)) {
                                Toast.makeText(context, "Fecha debe ser YYYY-MM-DD", Toast.LENGTH_LONG).show()
                                return@IconButton
                            }
                            body["fecha_evento"] = f
                        }

                        val h = hora.trim()
                        if (h.isNotEmpty()) {
                            body["hora_evento"] = ensureSeconds(h)
                        }

                        if (body.isEmpty()) {
                            Toast.makeText(context, "No hay cambios", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        scope.launch {
                            try {
                                val api = RetrofitInstance.create(context)
                                val resp = api.updateEventoPartial(id, body)
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Evento actualizado", Toast.LENGTH_SHORT).show()
                                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_evento", true)
                                    navController.popBackStack()
                                } else {
                                    val err = resp.errorBody()?.string()
                                    Toast.makeText(context, "No se pudo actualizar: $err", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (evento == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se encontró el evento")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción") },
                    minLines = 3
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Fecha (YYYY-MM-DD)") }
                    )
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Hora (HH:mm:ss)") }
                    )
                }
                OutlinedTextField(
                    value = precioText,
                    onValueChange = { precioText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { datePicker.show() }, modifier = Modifier.weight(1f)) { Text("Elegir fecha") }
                    Button(onClick = { timePicker.show() }, modifier = Modifier.weight(1f)) { Text("Elegir hora") }
                }
            }
        }
    }
}

private fun isYyyyMmDd(s: String) =
    Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(s.trim())

private fun ensureSeconds(hhmm: String): String {
    val t = hhmm.trim()
    return when {
        Regex("^\\d{2}:\\d{2}:\\d{2}$").matches(t) -> t
        Regex("^\\d{2}:\\d{2}$").matches(t) -> "$t:00"
        else -> t
    }
}

private fun extractDateYMD(value: Any?): String? {
    val raw: String? = when (value) {
        is String -> {
            if (Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(value)) {
                value
            } else {
                val patterns = listOf(
                    "yyyy-MM-dd'T'HH:mm:ssX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd HH:mm:ss"
                )
                var out: String? = null
                for (p in patterns) {
                    try {
                        val inFmt = java.text.SimpleDateFormat(p, java.util.Locale.US).apply {
                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }
                        val d = inFmt.parse(value) ?: continue
                        val outFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
                            timeZone = java.util.TimeZone.getDefault()
                        }
                        out = outFmt.format(d)
                        break
                    } catch (_: Exception) { }
                }
                out
            }
        }
        is Map<*, *> -> (value["\$date"] ?: value["date"]) as? String
        else -> null
    }

    if (raw == null) return null
    if (Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(raw)) return raw

    return try {
        val inFmt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val d = inFmt.parse(raw)
        val outFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getDefault()
        }
        if (d != null) outFmt.format(d) else raw
    } catch (_: Exception) {
        raw
    }
}
