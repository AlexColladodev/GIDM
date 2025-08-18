package com.example.hangout.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hangout.R
import androidx.compose.foundation.layout.size


sealed class BottomNavItem(val title: String, val route: String, val iconRes: Int) {
    object Home : BottomNavItem("Inicio", "inicio_usuario_generico", R.drawable.hangout_icon)
    object Calendar : BottomNavItem("Calendario", "eventos_actividades", R.drawable.calendario_nav)
    object Profile : BottomNavItem("Perfil", "mi_perfil", R.drawable.usuario_nav)
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Calendar, BottomNavItem.Profile)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute?.startsWith(item.route) == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
