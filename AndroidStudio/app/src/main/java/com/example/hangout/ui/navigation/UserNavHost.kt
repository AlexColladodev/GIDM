package com.example.hangout.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hangout.ui.components.BottomNavBar
import com.example.hangout.ui.screens.usuario.InicioUsuarioScreen
import androidx.compose.foundation.layout.padding

@Composable
fun UserNavHost(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "inicio_usuario",
            modifier = Modifier.padding(padding)
        ) {
            composable("inicio_usuario") { InicioUsuarioScreen(navController) }
            composable("eventos_actividades") { InicioUsuarioScreen(navController) }
            composable("mi_perfil") { InicioUsuarioScreen(navController) }
        }
    }
}
