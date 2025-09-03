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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearOfertaScreen(
    navController: NavController,
    establecimientoId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    val scroll = rememberScrollState()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImage = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Oferta") },
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
                label = { Text("Nombre de la oferta") },
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción") },
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    .height(170.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            if (nombre.isBlank() || descripcion.isBlank() || precio.isBlank()) {
                                Toast.makeText(context, "Completa nombre, descripción y precio", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            val api = RetrofitInstance.create(context)

                            val nombreRb = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                            val descripcionRb = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
                            val precioRb = precio.toRequestBody("text/plain".toMediaTypeOrNull())
                            val estIdRb = establecimientoId.toRequestBody("text/plain".toMediaTypeOrNull())

                            val imagenPart = selectedImage?.let { uri ->
                                val cr = context.contentResolver
                                val type = cr.getType(uri) ?: "image/*"
                                val bytes = cr.openInputStream(uri)?.use(InputStream::readBytes) ?: ByteArray(0)
                                val filename = cr.query(uri, null, null, null, null)?.use { c ->
                                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                    if (idx >= 0 && c.moveToFirst()) c.getString(idx) else uri.lastPathSegment ?: "oferta.jpg"
                                } ?: (uri.lastPathSegment ?: "oferta.jpg")
                                val body: RequestBody = bytes.toRequestBody(type.toMediaTypeOrNull())
                                MultipartBody.Part.createFormData("imagen", filename, body)
                            }

                            val resp = api.crearNuevaOfertaMultipart(
                                nombre = nombreRb,
                                descripcion = descripcionRb,
                                precio = precioRb,
                                idEstablecimiento = estIdRb,
                                imagen = imagenPart
                            )
                            if (!resp.isSuccessful || resp.body() == null) {
                                Toast.makeText(context, "No se pudo crear la oferta", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            val json = JSONObject(resp.body()!!.string())
                            val ofertaId = json.optString("id_oferta", "")
                            if (ofertaId.isBlank()) {
                                Toast.makeText(context, "Respuesta inválida del servidor", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            navController.navigate("datos_oferta_admin/$ofertaId") {
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Crear oferta") }
        }
    }
}

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return "http://127.0.0.1:5000/_uploads/photos/default.png"
    if (path.startsWith("http")) return path
    return "http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}"
}

/*
private fun buildImageUrl(path: String): String {
    if (path.startsWith("http")) return emulatorize(path)
    return emulatorize("http://127.0.0.1:5000${if (path.startsWith("/")) path else "/$path"}")
}
*/

private fun emulatorize(url: String): String = url.replace("127.0.0.1", "10.0.2.2")
