package com.example.myapplication.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.R

sealed class Screen(
    val route: String, 
    val title: String, 
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null
) {
    object Splash : Screen("splash", "Splash")
    
    object Home : Screen("home", "Inicio", iconRes = R.drawable.ic_home_icon)
    object Cart : Screen("cart", "Carrito", iconRes = R.drawable.ic_cart_icon)
    object Account : Screen("account", "Cuenta", iconRes = R.drawable.ic_user_icon)
    object Checkout : Screen("checkout", "Checkout")
    object Orders : Screen("orders", "Mis Pedidos")
    
    object Login : Screen("login", "Login", icon = Icons.Default.AccountCircle)
    object Register : Screen("register", "Register", icon = Icons.Default.AccountCircle)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Cart,
    Screen.Account
)