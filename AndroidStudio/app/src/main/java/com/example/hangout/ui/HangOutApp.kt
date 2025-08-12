package com.example.hangout.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hangout.ui.screens.InicioScreen
import com.example.hangout.ui.screens.RegistroScreen
import com.example.hangout.ui.screens.LoginScreen
import com.example.hangout.ui.screens.usuario.InicioUsuarioScreen
import com.example.hangout.ui.screens.usuario.DatosEstablecimientoScreen
import com.example.hangout.ui.screens.usuario.DatosOfertaScreen
import com.example.hangout.ui.screens.usuario.DatosEventoScreen

@Composable
fun HangOutApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "inicio") {
        composable("inicio") { InicioScreen(navController) }
        composable("registro") { RegistroScreen(navController) }
        composable("login") {  LoginScreen(navController)  }
        composable("inicio_usuario_generico") { InicioUsuarioScreen(navController) }
        composable("inicio_usuario_establecimiento/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DatosEstablecimientoScreen(navController, id)
        }
        composable("datos_oferta") { DatosOfertaScreen(navController) }
        composable("datos_evento") { DatosEventoScreen(navController) }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHangOutApp() {
    HangOutApp()
}
