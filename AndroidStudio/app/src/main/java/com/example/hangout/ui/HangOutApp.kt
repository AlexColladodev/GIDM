package com.example.hangout.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.hangout.ui.components.BottomNavBar
import com.example.hangout.ui.screens.InicioScreen
import com.example.hangout.ui.screens.RegistroScreen
import com.example.hangout.ui.screens.LoginScreen
import com.example.hangout.ui.screens.usuario.InicioUsuarioScreen
import com.example.hangout.ui.screens.usuario.DatosEstablecimientoScreen
import com.example.hangout.ui.screens.usuario.DatosOfertaScreen
import com.example.hangout.ui.screens.usuario.DatosEventoScreen
import com.example.hangout.ui.screens.usuario.EventosActividadesScreen
import androidx.compose.foundation.layout.Box

@Composable
fun HangOutApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    val routesWithoutBottomBar = setOf("inicio", "registro", "login")

    Scaffold(
        bottomBar = {
            if (currentRoute !in routesWithoutBottomBar) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier.padding(padding)
        ) {
            composable("inicio") { InicioScreen(navController) }
            composable("registro") { RegistroScreen(navController) }
            composable("login") { LoginScreen(navController) }


            composable("inicio_usuario_generico") { InicioUsuarioScreen(navController) }

            composable(
                route = "inicio_usuario_establecimiento/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                DatosEstablecimientoScreen(navController, id)
            }

            composable("datos_oferta") { DatosOfertaScreen(navController) }
            composable("datos_evento") { DatosEventoScreen(navController) }

            composable("eventos_actividades") {
                EventosActividadesScreen(navController)
            }
            composable("mi_perfil") {
                PlaceholderCenter("Mi Perfil (placeholder)")
            }
        }
    }
}

@Composable
private fun PlaceholderCenter(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHangOutApp() {
    HangOutApp()
}
