package com.example.hangout.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hangout.R
import com.example.hangout.models.LoginRequest
import com.example.hangout.network.RetrofitInstance
import com.example.hangout.ui.components.CircleBackground
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombreUsuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        CircleBackground() // 👈 fondo detrás

        Scaffold(
            containerColor = Color.Transparent, // 👈 permite ver el fondo
            topBar = {
                TopAppBar(
                    title = { Text("Iniciar Sesión") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val imageId = context.resources.getIdentifier("hangoutlogo", "drawable", context.packageName)

                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = "Logo",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = { nombreUsuario = it },
                    label = { Text("Nombre Usuario") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val credentials = LoginRequest(nombreUsuario, password)
                                val api = RetrofitInstance.create(context)
                                val response = api.login(credentials)

                                val responseBody = response.body()?.string()
                                if (responseBody.isNullOrEmpty()) {
                                    Toast.makeText(context, "Respuesta vacía", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                val json = JSONObject(responseBody)
                                val acceso = json.optBoolean("acceso", false)
                                val token = json.optString("token", "")
                                val rol = json.optString("rol", "")

                                if (!acceso || token.isEmpty()) {
                                    Toast.makeText(context, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("token", token)
                                    .apply()

                                when (rol) {
                                    "usuario_generico" -> navController.navigate("inicio_usuario_generico")
                                    "administrador_establecimiento" -> navController.navigate("inicio_admin_establecimiento")
                                    else -> Toast.makeText(context, "Rol desconocido", Toast.LENGTH_SHORT).show()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Sesión", color = Color.White)
                }
            }
        }
    }
}
