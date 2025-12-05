package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.models.CartItem
import com.example.myapplication.utils.PriceFormatter
import com.example.myapplication.viewmodel.CartViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onNavigateToCheckout: () -> Unit = {}
) {
    val context = LocalContext.current
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartTotal by cartViewModel.cartTotal.collectAsState()

    val subtotal = cartTotal
    val impuestos = if (subtotal > 0) 500 else 0
    val total = subtotal + impuestos
    var expanded by remember { mutableStateOf(true) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 200.dp)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .padding(bottom = 110.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(visible = expanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:", fontSize = 16.sp, color = Color.Gray)
                                Text(PriceFormatter.formatPrice(subtotal), fontSize = 16.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Impuestos:", fontSize = 16.sp, color = Color.Gray)
                                Text(PriceFormatter.formatPrice(impuestos), fontSize = 16.sp)
                            }
                            Divider(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            PriceFormatter.formatPrice(total),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToCheckout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Procesar Pago", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            Text(
                "Carrito",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
            )

            if (cartItems.isEmpty()) {
                EmptyCartView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 15.dp, bottom = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onIncrement = {
                                if (item.quantity >= item.manga.stock) {
                                    // Mostrar mensaje de stock máximo
                                } else {
                                    cartViewModel.updateQuantity(item.manga.id, item.quantity + 1)
                                }
                            },
                            onDecrement = {
                                cartViewModel.updateQuantity(item.manga.id, item.quantity - 1)
                            },
                            onDelete = {
                                cartViewModel.removeFromCart(item.manga.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartView() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Tu carrito está vacío", fontSize = 20.sp, color = Color.Gray)
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    val manga = item.manga
    val price = manga.salePrice ?: manga.price
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(manga.poster)
                    .crossfade(true)
                    .build(),
                contentDescription = manga.name,
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Información
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        manga.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        color = Color.Black
                    )
                    
                    Text(
                        manga.type ?: "",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        PriceFormatter.formatPrice(price),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )

                    // Controles de cantidad
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrementar",
                                tint = Color.Black
                            )
                        }

                        Text(
                            "${item.quantity}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier.size(32.dp),
                            enabled = item.quantity < manga.stock
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Incrementar",
                                tint = if (item.quantity < manga.stock) Color.Black else Color.Gray
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}
