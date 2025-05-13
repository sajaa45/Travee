package com.example.travee.ui.components


import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomNavBar(navController: NavController, selectedItem: Int) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = Color(0xFF2D3B41)
    ) {
        BottomNavItem(
            selected = selectedItem == 0,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            contentDescription = "Home",
            onClick = { navController.navigate("home") }
        )

        BottomNavItem(
            selected = selectedItem == 1,
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            contentDescription = "Explore",
            onClick = { navController.navigate("search") }
        )

        BottomNavItem(
            selected = selectedItem == 2,
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.Favorite,
            contentDescription = "Favorites",
            onClick = { navController.navigate("favorites") }
        )

        BottomNavItem(
            selected = selectedItem == 3,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            contentDescription = "Profile",
            onClick = { navController.navigate("profile") }
        )
    }
}

@Composable
fun RowScope.BottomNavItem(
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = contentDescription
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1EBFC3),
            unselectedIconColor = Color.Gray,
            indicatorColor = Color(0xFF2D3B41)
        )
    )
}
