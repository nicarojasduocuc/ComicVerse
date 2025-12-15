package com.example.myapplication.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.*
import com.example.myapplication.utils.UserSession
import com.example.myapplication.viewmodel.CartViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // ViewModel compartido del carrito
    val cartViewModel: CartViewModel = viewModel()
    
    // Estado para detectar cuando se actualiza la necesidad de navegar
    var checkNavigation by remember { mutableStateOf(0) }
    
    // Verificar continuamente si necesita navegar a Orders
    LaunchedEffect(currentDestination, checkNavigation) {
        if (UserSession.needsNavigateToOrders(context)) {
            android.util.Log.d("AppNavigation", "✅ Navegando a Orders después del pago")
            
            // Navegar a Orders limpiando todo el back stack
            navController.navigate(Screen.Orders.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    
    // Recheck periódicamente
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            checkNavigation++
        }
    }

    val screensWithBottomBar = listOf(
        Screen.Home.route,
        Screen.Cart.route,
        Screen.Account.route,
        "detail/{productId}"
    )

    val showBottomBar = screensWithBottomBar.any { route ->
        if (route.contains("{")) {
            currentDestination?.route?.startsWith(route.substringBefore("{")) == true
        } else {
            currentDestination?.route == route
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
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
                                launchSingleTop = true
                            }
                        },
                        onProductClick = { productId ->
                            navController.navigate("detail/$productId")
                        }
                    )
                }
                
                composable(
                    route = "detail/{productId}",
                    arguments = listOf(
                        navArgument("productId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId") ?: ""
                    DetailScreen(
                        productId = productId,
                        cartViewModel = cartViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                
                composable(Screen.Cart.route) { 
                    CartScreen(
                        cartViewModel = cartViewModel,
                        onNavigateToCheckout = {
                            navController.navigate(Screen.Checkout.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Checkout.route) {
                    CheckoutScreen(
                        cartViewModel = cartViewModel,
                        onNavigateBack = { 
                            navController.popBackStack() 
                        },
                        onNavigateToOrders = {
                            navController.navigate(Screen.Orders.route) {
                                popUpTo(Screen.Checkout.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Account.route) {
                    if (UserSession.isLoggedIn(context)) {
                        AccountScreen(
                            onLogout = {
                                UserSession.logout(context)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onNavigateToOrders = {
                                navController.navigate(Screen.Orders.route) {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToAdmin = {
                                navController.navigate(Screen.Admin.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
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
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
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
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Orders.route) {
                    OrdersScreen()
                }
                
                composable(Screen.Admin.route) {
                    AdminScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        }
                    )
                }
            }
        }

        if (showBottomBar) {
            FloatingNavBar(navController, currentDestination)
        }
    }
}

@Composable
fun FloatingNavBar(navController: NavHostController, currentDestination: NavDestination?) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 40.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .width(0.9f.percentOfScreen())
                .height(70.dp)
                .graphicsLayer {
                    shadowElevation = 35f
                    translationY = -4f
                    shape = RoundedCornerShape(40.dp)
                    clip = true
                }
                .clip(RoundedCornerShape(40.dp))
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == screen.route } == true

                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    bounded = true,
                                    radius = 666.dp,
                                    color = Color(0x33FF9800)
                                )
                            ) {
                                val goToLogin = screen.route == Screen.Account.route &&
                                        !UserSession.isLoggedIn(context)
                                val destination =
                                    if (goToLogin) Screen.Login.route else screen.route

                                navController.navigate(destination) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = screen.iconRes!!),
                            contentDescription = screen.title,
                            tint = if (selected) Color(0xFFFF9800) else Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = screen.title,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Float.percentOfScreen(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    return (this * screenWidth.value).dp
}