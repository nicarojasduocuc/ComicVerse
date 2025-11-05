package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.myapplication.db.models.Product
import com.example.myapplication.utils.PriceFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    productId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var product by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf(1) }

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
                    .background(Color.White)
                    .padding(padding)
            ) {
                // 🖼️ Imagen de fondo difuminada
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(prod.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .align(Alignment.TopCenter)
                )

                // 🌫️ Gradiente superior (blanco → transparente)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                        .zIndex(2f)
                )

                // 🌫️ Gradiente inferior (transparente → blanco)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                        .offset(y = 250.dp)
                        .zIndex(2f)
                )

                // 📖 Contenido scrolleable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 80.dp)
                ) {
                    // 🔙 Header: Volver + Favorito
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .zIndex(10f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onNavigateBack() }
                                .padding(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back_icon),
                                contentDescription = "Volver",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Black
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Volver",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(onClick = { /* TODO: Agregar a favoritos */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_heart_icon),
                                contentDescription = "Favorito",
                                modifier = Modifier.size(28.dp),
                                tint = Color.Black
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // 📘 Imagen principal del producto (centrada)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(240.dp)
                                .height(360.dp),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 20.dp,
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

                    Spacer(Modifier.height(32.dp))

                    // 📝 Información del producto
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 24.dp)
                    ) {
                        // Categoría y Año
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Categoría: ",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = prod.type,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF9800),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Año: ${prod.year}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Nombre del producto
                        Text(
                            text = prod.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            lineHeight = 28.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        // Stock disponible
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (prod.stock > 10) Color.Green
                                        else if (prod.stock > 0) Color(0xFFFFB300)
                                        else Color.Red
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

                        // Descripción
                        Text(
                            text = "Descripción:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = prod.description,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(150.dp))
                    }
                }

                // ⬇️ Bottom bar fija con precio y botón
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp),
                    color = Color.White,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        // Precio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                if (prod.salePrice != null && prod.salePrice!! < prod.price) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = PriceFormatter.formatPrice(prod.salePrice!!),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = PriceFormatter.formatPrice(prod.price),
                                            fontSize = 16.sp,
                                            color = Color.Gray,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    }
                                } else {
                                    Text(
                                        text = PriceFormatter.formatPrice(prod.price),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Controles de cantidad y botón
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Controles de cantidad
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFFFF3E0),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_minus_icon),
                                            contentDescription = "Disminuir",
                                            tint = Color(0xFFFF9800)
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
                                        modifier = Modifier.size(36.dp),
                                        enabled = quantity < prod.stock
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_plus_icon),
                                            contentDescription = "Aumentar",
                                            tint = if (quantity < prod.stock) Color(0xFFFF9800) else Color.Gray
                                        )
                                    }
                                }
                            }

                            // Botón agregar al carrito
                            Button(
                                onClick = {
                                    scope.launch {
                                        val existingItem = db.cartDao().getCartItem(1, prod.id)
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
                                                    userId = 1,
                                                    quantity = quantity
                                                )
                                            )
                                            snackbarHostState.showSnackbar(
                                                message = "Agregado al carrito",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(start = 12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Agregar Al Carrito",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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