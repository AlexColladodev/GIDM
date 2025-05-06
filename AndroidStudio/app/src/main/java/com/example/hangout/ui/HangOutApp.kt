package com.example.hangout.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hangout.ui.screens.InicioScreen

@Composable
fun HangOutApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "inicio") {
        composable("inicio") { InicioScreen(navController) }
        composable("login") { /* LoginScreen(navController) */ }
        composable("registro") { /* RegistroScreen(navController) */ }
        // Añadir más pantallas aquí
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHangOutApp() {
    HangOutApp()
}
