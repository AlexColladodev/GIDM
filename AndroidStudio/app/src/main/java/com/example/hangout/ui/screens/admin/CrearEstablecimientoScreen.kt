package com.example.hangout.ui.screens.admin

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.hangout.data.AmbientesProvider
import com.example.hangout.models.Ambiente
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.InputStream
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEstablecimientoScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("") }
    var cif by remember { mutableStateOf("") }
    var selectedAmbientes by remember { mutableStateOf(setOf<Ambiente>()) }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    val scroll = rememberScrollState()

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> selectedImage = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Establecimiento") },
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
                label = { Text("Nombre del establecimiento") },
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = cif,
                onValueChange = { cif = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("CIF") },
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { pickImage.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (selectedImage != null) "Cambiar imagen" else "Seleccionar imagen")
            }

            val preview = selectedImage ?: Uri.parse(buildImageUrl("/_uploads/photos/default_establecimiento.png"))
            AsyncImage(
                model = preview,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Text("Ambiente")
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
                                    .size(80.dp)
                                    .clickable {
                                        selectedAmbientes = if (seleccionado) selectedAmbientes - ambiente else selectedAmbientes + ambiente
                                    }
                            ) {
                                Surface(
                                    tonalElevation = if (seleccionado) 4.dp else 0.dp,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(ambiente.imageRes),
                                        contentDescription = ambiente.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(8.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(ambiente.name)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            if (nombre.isBlank() || cif.isBlank()) {
                                Toast.makeText(context, "Completa nombre y CIF", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            val api = RetrofitInstance.create(context)

                            val nombreRb = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                            val cifRb = cif.toRequestBody("text/plain".toMediaTypeOrNull())
                            val ambientesRb = selectedAmbientes
                                .map { normalizeText(it.name) }
                                .joinToString(",")
                                .toRequestBody("text/plain".toMediaTypeOrNull())

                            val imagenPart = selectedImage?.let { uri ->
                                val cr = context.contentResolver
                                val type = cr.getType(uri) ?: "image/*"
                                val bytes = cr.openInputStream(uri)?.use(InputStream::readBytes) ?: ByteArray(0)
                                val filename = cr.query(uri, null, null, null, null)?.use { c ->
                                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                    if (idx >= 0 && c.moveToFirst()) c.getString(idx) else uri.lastPathSegment ?: "imagen.jpg"
                                } ?: (uri.lastPathSegment ?: "imagen.jpg")
                                val body: RequestBody = bytes.toRequestBody(type.toMediaTypeOrNull())
                                MultipartBody.Part.createFormData("imagen", filename, body)
                            }

                            val resp = api.crearNuevoEstablecimientoMultipart(
                                nombre = nombreRb,
                                cif = cifRb,
                                ambiente = ambientesRb,
                                imagen = imagenPart
                            )

                            if (!resp.isSuccessful || resp.body() == null) {
                                Toast.makeText(context, "No se pudo crear", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            val created = JSONObject(resp.body()!!.string())
                            val id = created.optString("id", "")
                            if (id.isBlank()) {
                                Toast.makeText(context, "Respuesta inválida", Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            navController.navigate("admin_establecimiento_detalle?data=$id") {
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear")
            }
        }
    }
}

private fun buildImageUrl(path: String): String {
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize("http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}")
}

private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")

private fun normalizeText(s: String): String {
    val n = Normalizer.normalize(s.lowercase().trim(), Normalizer.Form.NFD)
    return n.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}
