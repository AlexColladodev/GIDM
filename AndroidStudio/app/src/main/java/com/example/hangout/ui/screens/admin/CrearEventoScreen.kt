package com.example.hangout.ui.screens.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEventoScreen(
    navController: NavController,
    establecimientoId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    val scroll = rememberScrollState()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImage = uri
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Evento") },
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
                .verticalScroll(scroll)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre del evento") }
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción") },
                minLines = 3
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    PickerCard(
                        label = "Fecha (YYYY-MM-DD)",
                        value = fecha,
                        onClick = { datePicker.show() }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    PickerCard(
                        label = "Hora (HH:MM:SS)",
                        value = hora,
                        onClick = { timePicker.show() }
                    )
                }
            }
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Button(
                onClick = { pickImage.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (selectedImage != null) "Cambiar imagen" else "Seleccionar imagen") }
            val preview = selectedImage ?: Uri.parse(buildImageUrl("/_uploads/photos/default_establecimiento.png"))
            AsyncImage(
                model = preview,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            if (nombre.isBlank() || fecha.isBlank() || hora.isBlank() || precio.isBlank()) {
                                Toast.makeText(context, "Completa nombre, fecha, hora y precio", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            if (establecimientoId.isBlank()) {
                                Toast.makeText(context, "Falta id_establecimiento", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            val precioNormalizado = precio.replace(',', '.')
                            val api = RetrofitInstance.create(context)
                            val nombreRb = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                            val descripcionRb = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
                            val precioRb = precioNormalizado.toRequestBody("text/plain".toMediaTypeOrNull())
                            val horaRb = ensureSeconds(hora).toRequestBody("text/plain".toMediaTypeOrNull())
                            val fechaRb = fecha.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                            val estIdRb = establecimientoId.toRequestBody("text/plain".toMediaTypeOrNull())
                            val imagenPart = selectedImage?.let { uri ->
                                val cr = context.contentResolver
                                val type = cr.getType(uri) ?: "image/*"
                                val bytes = cr.openInputStream(uri)?.use(InputStream::readBytes) ?: ByteArray(0)
                                val filename = cr.query(uri, null, null, null, null)?.use { c ->
                                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                    if (idx >= 0 && c.moveToFirst()) c.getString(idx) else uri.lastPathSegment ?: "evento.jpg"
                                } ?: (uri.lastPathSegment ?: "evento.jpg")
                                val body: RequestBody = bytes.toRequestBody(type.toMediaTypeOrNull())
                                MultipartBody.Part.createFormData("imagen", filename, body)
                            }
                            val resp = api.crearNuevoEventoMultipart(
                                nombre = nombreRb,
                                descripcion = descripcionRb,
                                precio = precioRb,
                                hora = horaRb,
                                fecha = fechaRb,
                                idEstablecimiento = estIdRb,
                                imagen = imagenPart
                            )
                            if (!resp.isSuccessful || resp.body() == null) {
                                Toast.makeText(context, "No se pudo crear el evento", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            val json = JSONObject(resp.body()!!.string())
                            val eventoId = json.optString("id_evento", "")
                            if (eventoId.isBlank()) {
                                Toast.makeText(context, "Respuesta inválida del servidor", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            navController.navigate("datos_evento_admin/$eventoId") { launchSingleTop = true }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Crear evento") }
        }
    }
}

@Composable
private fun PickerCard(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    if (value.isBlank()) "Seleccionar…" else value,
                    color = if (value.isBlank())
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun ensureSeconds(hhmm: String): String {
    val t = hhmm.trim()
    return when {
        Regex("^\\d{2}:\\d{2}:\\d{2}$").matches(t) -> t
        Regex("^\\d{2}:\\d{2}$").matches(t) -> "$t:00"
        else -> t
    }
}

private fun buildImageUrl(path: String): String {
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize("http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}")
}

private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
