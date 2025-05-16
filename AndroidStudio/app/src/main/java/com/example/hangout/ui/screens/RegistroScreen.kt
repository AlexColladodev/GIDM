package com.example.hangout.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.hangout.models.Ambiente
import com.example.hangout.data.AmbientesProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var tipoCuenta by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var selectedAmbientes by remember { mutableStateOf(setOf<Ambiente>()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro") },
                navigationIcon = {
                    IconButton(onClick = { /* navController.popBackStack() */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = nombreUsuario, onValueChange = { nombreUsuario = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = contraseña, onValueChange = { contraseña = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Text("Tipo de cuenta:")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Usuario", "Administrador").forEach { tipo ->
                    val selected = tipoCuenta == tipo
                    OutlinedButton(
                        onClick = { tipoCuenta = tipo },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(tipo)
                    }
                }
            }

            if (tipoCuenta == "Administrador") {
                OutlinedTextField(
                    value = dni,
                    onValueChange = { dni = it },
                    label = { Text("DNI") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (tipoCuenta == "Usuario") {
                Text("Ambiente:", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

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
                                            selectedAmbientes = if (seleccionado)
                                                selectedAmbientes - ambiente
                                            else
                                                selectedAmbientes + ambiente
                                        }
                                        .padding(4.dp)
                                ) {
                                    Surface(
                                        tonalElevation = if (seleccionado) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (seleccionado) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
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
            }

            Button(onClick = { launcher.launch("image/*") }) {
                Text("Seleccionar imagen de perfil")
            }

            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        Toast.makeText(context, "Registro pendiente de implementación", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Registrarse")
            }
        }
    }
}
