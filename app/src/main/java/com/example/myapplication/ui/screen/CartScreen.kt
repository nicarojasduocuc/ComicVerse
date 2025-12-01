package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.CartItemWithProduct
import com.example.myapplication.utils.PriceFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateToCheckout: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val cartItems by db.cartDao().getCartWithProducts(userId = 1)
        .collectAsState(initial = emptyList())

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val impuestos = if (subtotal > 0) 500.0 else 0.0
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
                    IconButton(onClick = { expanded = !expanded }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_chevron_icon),
                            contentDescription = "Expandir / Colapsar",
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(if (expanded) 180f else 0f)
                        )
                    }

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

            if (cartItems.isEmpty()) EmptyCartView() else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 15.dp, bottom = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onIncrement = {
                                scope.launch {
                                    db.cartDao().getCartItem(1, item.id)?.let { cartItem ->
                                        if (cartItem.quantity >= item.stock) {
                                            snackbarHostState.showSnackbar(
                                                message = "Stock máximo alcanzado (${item.stock} unidades)",
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            db.cartDao().updateCartItem(
                                                cartItem.copy(quantity = cartItem.quantity + 1)
                                            )
                                        }
                                    }
                                }
                            },
                            onDecrement = {
                                scope.launch {
                                    db.cartDao().getCartItem(1, item.id)?.let {
                                        if (it.quantity > 1)
                                            db.cartDao().updateCartItem(it.copy(quantity = it.quantity - 1))
                                        else db.cartDao().deleteCartItem(it)
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    db.cartDao().getCartItem(1, item.id)?.let {
                                        db.cartDao().deleteCartItem(it)
                                    }
                                }
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
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrito vacío",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFFF9800)
            )

            Spacer(Modifier.height(8.dp))
            Text("Tu carrito está vacío", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Agrega productos desde la tienda", color = Color.Gray)
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItemWithProduct,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(
                Modifier
                    .weight(1f)
                    .height(150.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            maxLines = 2,
                            lineHeight = 24.sp
                        )
                        if (item.type.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                item.type,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            PriceFormatter.formatPrice(item.price),
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_delete_icon),
                            contentDescription = "Eliminar"
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier
                        .height(44.dp)
                        .width(140.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_minus_icon),
                                contentDescription = "Disminuir",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "${item.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier.size(28.dp),
                            enabled = item.quantity < item.stock
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_plus_icon),
                                contentDescription = "Aumentar",
                                tint = if (item.quantity < item.stock) Color(0xFFFF9800) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}