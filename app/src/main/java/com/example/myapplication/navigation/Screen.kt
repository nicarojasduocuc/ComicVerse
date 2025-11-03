package com.example.myapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingCart)
    object Account : Screen("account", "Cuenta", Icons.Default.AccountCircle)
    object Login : Screen("login", "Login", Icons.Default.AccountCircle)
    object Register : Screen("register", "Register", Icons.Default.AccountCircle)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Cart,
    Screen.Account
)