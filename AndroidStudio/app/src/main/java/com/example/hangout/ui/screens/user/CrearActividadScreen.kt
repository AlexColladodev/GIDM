package com.example.hangout.ui.screens.usuario

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hangout.network.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.Calendar

data class NuevaActividadRequest(
    val nombre_actividad: String,
    val descripcion_actividad: String,
    val ubicacion: String,
    val fecha_actividad: String,
    val hora_actividad: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearActividadScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val cal = Calendar.getInstance()

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
                title = { Text("Crear Actividad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            FieldCard(
                label = "Título de la Actividad",
                value = nombre,
                onChange = { nombre = it },
                minHeight = 56.dp
            )

            FieldCard(
                label = "Descripción",
                value = descripcion,
                onChange = { descripcion = it },
                minHeight = 140.dp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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

            FieldCard(
                label = "Ubicación",
                value = ubicacion,
                onChange = { ubicacion = it },
                minHeight = 56.dp
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (nombre.isBlank() || descripcion.isBlank() || ubicacion.isBlank() || fecha.isBlank() || hora.isBlank()) {
                            snack.showSnackbar("Completa todos los campos")
                            return@launch
                        }
                        loading = true
                        val api = RetrofitInstance.create(context)
                        val body = NuevaActividadRequest(
                            nombre_actividad = nombre.trim(),
                            descripcion_actividad = descripcion.trim(),
                            ubicacion = ubicacion.trim(),
                            fecha_actividad = fecha,
                            hora_actividad = hora
                        )
                        val resp: Response<ResponseBody> = try {
                            api.crearNuevaActividadGateway(body)
                        } catch (e: Exception) {
                            loading = false
                            snack.showSnackbar("Error de red")
                            return@launch
                        }
                        loading = false
                        if (resp.isSuccessful) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("reload_actividades", true)
                            navController.popBackStack()
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Crear Actividad")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/* ---------- UI helpers con estética tipo Surface + BasicTextField ---------- */

@Composable
private fun FieldCard(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    minHeight: androidx.compose.ui.unit.Dp
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { inner ->
                        if (value.isBlank()) {
                            Text(
                                "Escribe aquí…",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        inner()
                    }
                )
            }
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
        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
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
