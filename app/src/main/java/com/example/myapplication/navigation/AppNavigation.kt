package com.example.myapplication.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.*
import com.example.myapplication.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screensWithBottomBar = listOf(
        Screen.Home.route,
        Screen.Cart.route,
        Screen.Account.route
    )

    val showBottomBar = currentDestination?.route in screensWithBottomBar

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                // Si se hace clic en "Cuenta" y no hay sesión, ir a Login
                                if (screen.route == Screen.Account.route && !UserSession.isLoggedIn(context)) {
                                    navController.navigate(Screen.Login.route) {
                                        launchSingleTop = true
                                    }
                                } else {
                                    // Navegación normal entre pantallas del bottom bar
                                    navController.navigate(screen.route) {
                                        // Limpiar back stack hasta Home para evitar ciclos
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        // Solo restaurar estado si no es Account (para forzar recarga)
                                        restoreState = screen.route != Screen.Account.route
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen()
            }
            composable(Screen.Account.route) {
                // Verificar si el usuario está logueado
                val isLoggedIn = UserSession.isLoggedIn(context)
                
                if (isLoggedIn) {
                    AccountScreen(
                        onLogout = {
                            UserSession.logout(context)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                } else {
                    // Si no está logueado, redirigir a Login
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Account.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}