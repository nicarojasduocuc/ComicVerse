package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.models.CartItem
import com.example.myapplication.db.models.Favorite
import com.example.myapplication.db.models.Product
import com.example.myapplication.utils.PriceFormatter
import com.example.myapplication.utils.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    productId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var product by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf(1) }
    val isLoggedIn = UserSession.isLoggedIn(context)
    val loggedUserId = UserSession.getUserId(context)
    val cartUserId = if (isLoggedIn) loggedUserId else 1
    val isFavorite by if (isLoggedIn && loggedUserId > 0) {
        db.favoriteDao().isFavorite(loggedUserId, productId)
            .collectAsState(initial = false)
    } else {
        remember { mutableStateOf(false) }
    }

    LaunchedEffect(productId) {
        product = db.productDao().getProductById(productId)
    }

    product?.let { prod ->
        Scaffold(
            containerColor = Color.White,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 150.dp)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(prod.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.4f)
                                .zIndex(1f)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color.Transparent
                                        )
                                    )
                                )
                                .align(Alignment.TopCenter)
                                .zIndex(2f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .zIndex(10f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onNavigateBack() }
                                    .padding(0.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_back_icon),
                                    contentDescription = "Volver",
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.Black
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Volver",
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (!isLoggedIn) {
                                        onNavigateToLogin()
                                    } else {
                                        scope.launch {
                                            val existingFavorite = db.favoriteDao()
                                                .getFavorite(loggedUserId, productId)
                                            if (existingFavorite != null) {
                                                db.favoriteDao().deleteFavorite(existingFavorite)
                                                snackbarHostState.showSnackbar(
                                                    message = "Eliminado de favoritos",
                                                    duration = SnackbarDuration.Short
                                                )
                                            } else {
                                                db.favoriteDao().insertFavorite(
                                                    Favorite(
                                                        userId = loggedUserId,
                                                        productId = productId
                                                    )
                                                )
                                                snackbarHostState.showSnackbar(
                                                    message = "Agregado a favoritos",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isFavorite)
                                            R.drawable.ic_heart_selected_icon
                                        else
                                            R.drawable.ic_heart_icon
                                    ),
                                    contentDescription = "Favorito",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(3f),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(217.dp)
                                    .height(325.dp),
                                shape = RoundedCornerShape(0.dp),
                                shadowElevation = 16.dp,
                                color = Color.White
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(prod.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = prod.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White
                                        )
                                    )
                                )
                                .align(Alignment.BottomCenter)
                                .zIndex(2f)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Categoría: ",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = prod.type,
                                    fontSize = 14.sp,
                                    color = Color(0xFFFF9800),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Año: ${prod.year}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = prod.name,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            lineHeight = 30.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (prod.stock > 10) Color(0xFF4CAF50)
                                        else if (prod.stock > 0) Color(0xFFFF9800)
                                        else Color(0xFFF44336)
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "En Stock: ${prod.stock} Unidades",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Descripción:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = prod.description,
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFFFF3E0),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_minus_icon),
                                            contentDescription = "Disminuir",
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = "$quantity",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.Black
                                    )
                                    IconButton(
                                        onClick = { if (quantity < prod.stock) quantity++ },
                                        modifier = Modifier.size(32.dp),
                                        enabled = quantity < prod.stock
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_plus_icon),
                                            contentDescription = "Aumentar",
                                            tint = if (quantity < prod.stock) Color(0xFFFF9800) else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val existingItem = db.cartDao().getCartItem(cartUserId, prod.id)
                                            if (existingItem != null) {
                                                val newQuantity = existingItem.quantity + quantity
                                                if (newQuantity <= prod.stock) {
                                                    db.cartDao().updateCartItem(
                                                        existingItem.copy(quantity = newQuantity)
                                                    )
                                                    snackbarHostState.showSnackbar(
                                                        message = "Cantidad actualizada",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                } else {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Stock insuficiente",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            } else {
                                                db.cartDao().insertCartItem(
                                                    CartItem(
                                                        productId = prod.id,
                                                        userId = cartUserId,
                                                        quantity = quantity
                                                    )
                                                )
                                                snackbarHostState.showSnackbar(
                                                    message = "Agregado al carrito",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(
                                                message = "Error al agregar al carrito",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Agregar Al Carrito",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFFF9800))
    }
}
