package com.example.hangout.ui

import androidx.compose.foundation.layout.Box
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hangout.ui.components.BottomNavBar
import com.example.hangout.ui.screens.InicioScreen
import com.example.hangout.ui.screens.LoginScreen
import com.example.hangout.ui.screens.RegistroScreen
import com.example.hangout.ui.screens.admin.InicioAdministradorScreen
import com.example.hangout.ui.screens.admin.CrearEstablecimientoScreen
import com.example.hangout.ui.screens.usuario.CrearActividadScreen
import com.example.hangout.ui.screens.usuario.CrearReviewScreen
import com.example.hangout.ui.screens.usuario.DatosActividadScreen
import com.example.hangout.ui.screens.usuario.DatosEstablecimientoScreen
import com.example.hangout.ui.screens.usuario.DatosEventoScreen
import com.example.hangout.ui.screens.usuario.DatosEventoScreen2
import com.example.hangout.ui.screens.usuario.DatosOfertaScreen
import com.example.hangout.ui.screens.usuario.DatosPerfilScreen
import com.example.hangout.ui.screens.usuario.EditarPerfilScreen
import com.example.hangout.ui.screens.usuario.EventosActividadesScreen
import com.example.hangout.ui.screens.usuario.InicioUsuarioScreen
import com.example.hangout.ui.screens.admin.DatosEstablecimientoScreen as AdminDatosEstablecimientoScreen
import com.example.hangout.ui.screens.admin.DatosOfertasScreen
import com.example.hangout.ui.screens.admin.DatosEventosScreen
import com.example.hangout.ui.screens.admin.CrearOfertaScreen
import com.example.hangout.ui.screens.admin.CrearEventoScreen
import com.example.hangout.ui.screens.admin.EditarEstablecimientoScreen
import com.example.hangout.ui.screens.admin.EditarOfertaScreen
import com.example.hangout.ui.screens.admin.EditarEventoScreen

@Composable
fun HangOutApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val routesHideExact = setOf("inicio", "registro", "login", "inicio_admin_establecimiento", "crear_establecimiento", "datos_oferta_admin", "crear_evento", "crear_oferta", "datos_evento_admin", "editar_evento", "editar_oferta", "editar_establecimiento")
    val routesHidePrefix = setOf("admin_establecimiento_detalle")
    val hideBottomBar = (currentRoute in routesHideExact) || routesHidePrefix.any { currentRoute?.startsWith(it) == true }

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
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
            composable("datos_evento2") { DatosEventoScreen2(navController) }
            composable("eventos_actividades") { EventosActividadesScreen(navController) }
            composable("datos_perfil") { DatosPerfilScreen(navController) }
            composable("editar_perfil") { EditarPerfilScreen(navController) }
            composable("crear_actividad") { CrearActividadScreen(navController) }
            composable("datos_actividad") { DatosActividadScreen(navController) }
            composable(
                route = "crearReview/{establecimientoId}/{initialRating}",
                arguments = listOf(
                    navArgument("establecimientoId") { type = NavType.StringType },
                    navArgument("initialRating") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val estId = backStackEntry.arguments?.getString("establecimientoId") ?: ""
                val init = backStackEntry.arguments?.getInt("initialRating") ?: 5
                CrearReviewScreen(navController, estId, init)
            }

            composable("inicio_admin_establecimiento") { InicioAdministradorScreen(navController) }
            composable("crear_establecimiento") { CrearEstablecimientoScreen(navController) }
            composable(
                route = "admin_establecimiento_detalle?data={data}",
                arguments = listOf(navArgument("data") { type = NavType.StringType; nullable = true })
            ) { backStack ->
                val data = backStack.arguments?.getString("data") ?: ""
                AdminDatosEstablecimientoScreen(navController, data)
            }
            composable(
                route = "crear_oferta/{establecimientoId}",
                arguments = listOf(navArgument("establecimientoId") { type = NavType.StringType })
            ) { backStack ->
                val estId = backStack.arguments?.getString("establecimientoId") ?: ""
                CrearOfertaScreen(navController, estId)
            }
            composable(
                route = "crear_evento/{establecimientoId}",
                arguments = listOf(navArgument("establecimientoId") { type = NavType.StringType })
            ) { backStack ->
                val estId = backStack.arguments?.getString("establecimientoId") ?: ""
                CrearEventoScreen(navController, estId)
            }
            composable(
                route = "datos_oferta_admin/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                DatosOfertasScreen(navController, id)
            }
            composable(
                route = "datos_evento_admin/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                DatosEventosScreen(navController, id)
            }
            composable(
                route = "editar_establecimiento/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                val id = it.arguments?.getString("id") ?: ""
                EditarEstablecimientoScreen(navController, id)
            }
            composable(
                route = "editar_oferta/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                EditarOfertaScreen(navController, id)
            }
            composable(
                route = "editar_evento/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                EditarEventoScreen(navController, id)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHangOutApp() {
    HangOutApp()
}
