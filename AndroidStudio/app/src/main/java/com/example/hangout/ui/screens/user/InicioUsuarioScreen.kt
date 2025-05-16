package com.example.hangout.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hangout.viewmodels.UsuariosGenericosViewModel

@Composable
fun InicioUsuarioScreen(
    navController: NavController,
    viewModel: UsuariosGenericosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Column {
                    Text(
                        text = "Establecimientos recomendados",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.preferidos.forEach {
                        Text("- ${it.nombre_establecimiento}")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            item {
                Column {
                    Text(
                        text = "Eventos próximos",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.eventos.forEach {
                        Text("- ${it.nombre_evento}")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            item {
                Column {
                    Text(
                        text = "Tus actividades",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.actividades.forEach {
                        Text("- ${it.nombre_actividad}")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}