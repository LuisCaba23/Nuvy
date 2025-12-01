package com.lccm.nuvy.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.lccm.nuvy.NuvyDestinations

data class NavItem(
    val route: String,
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
)

@Composable
fun NuvyBottomNavBar(
    currentDestination: String,
    onNavigate: (String) -> Unit
) {
    val navItems = listOf(
        NavItem(
            route = NuvyDestinations.HOME,
            label = "Inicio",
            iconFilled = Icons.Filled.Home,
            iconOutlined = Icons.Outlined.Home
        ),
        NavItem(
            route = NuvyDestinations.EDITOR,
            label = "Editor",
            iconFilled = Icons.Filled.Code,
            iconOutlined = Icons.Outlined.Code
        ),
        NavItem(
            route = NuvyDestinations.CONNECT,
            label = "Conexión",
            iconFilled = Icons.Filled.Link,
            iconOutlined = Icons.Outlined.Link
        )
    )

    NavigationBar {
        navItems.forEach { item ->
            val isSelected = currentDestination == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                label = { Text(item.label) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                        contentDescription = item.label
                    )
                }
            )
        }
    }
}