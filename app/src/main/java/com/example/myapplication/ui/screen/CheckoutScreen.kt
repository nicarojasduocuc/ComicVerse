package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.CartItemWithProduct
import com.example.myapplication.db.models.Order
import com.example.myapplication.db.models.OrderItem
import com.example.myapplication.utils.PriceFormatter
import com.example.myapplication.utils.UserSession
import kotlinx.coroutines.launch

enum class PaymentMethod {
    NONE, GOOGLE_PAY, CREDIT_CARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val userId = UserSession.getUserId(context)
    
    val cartItems by db.cartDao().getCartWithProducts(userId = userId)
        .collectAsState(initial = emptyList())
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isInitialLoad by remember { mutableStateOf(true) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.NONE) }
    var showAllProducts by remember { mutableStateOf(false) }
    
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val impuestos = if (subtotal > 0) 500.0 else 0.0
    val total = subtotal + impuestos

    var finalTotal by remember { mutableStateOf(0.0) }

    LaunchedEffect(cartItems) {
        if (isInitialLoad) {
            kotlinx.coroutines.delay(100)
            isInitialLoad = false
        } else {
            if (cartItems.isEmpty() && !showSuccessDialog) {
                onNavigateToCart()
            }
        }
    }

    if (isInitialLoad) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFFFF9800),
                modifier = Modifier.size(48.dp)
            )
        }
        return
    }

    if (cartItems.isEmpty() && !showSuccessDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "No hay productos en el carrito",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToCart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Volver al carrito")
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Cancelar",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_icon),
                            contentDescription = "Volver",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Resumen de compra",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (cartItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(cartItems[0].imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = cartItems[0].name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = cartItems[0].name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                maxLines = 1
                            )
                            Text(
                                text = "Cantidad: ${cartItems[0].quantity}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Text(
                        text = PriceFormatter.formatPrice(cartItems[0].price * cartItems[0].quantity),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            if (cartItems.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAllProducts = !showAllProducts }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_icon),
                        contentDescription = if (showAllProducts) "Ocultar" else "Mostrar todos",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (showAllProducts) 180f else 0f),
                        tint = Color.Black
                    )
                }

                AnimatedVisibility(
                    visible = showAllProducts,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        cartItems.drop(1).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(item.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Cantidad: ${item.quantity}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Text(
                                    text = PriceFormatter.formatPrice(item.price * item.quantity),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F8F8)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Subtotal:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            PriceFormatter.formatPrice(subtotal),
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Impuestos y cargos:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            PriceFormatter.formatPrice(impuestos),
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.LightGray
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total a pagar:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = PriceFormatter.formatPrice(total),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Elija un método de pago",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                "Seleccione el método de pago que más le convenga.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedPaymentMethod = PaymentMethod.GOOGLE_PAY
                    finalTotal = total
                    isProcessing = true
                    scope.launch {
                        try {
                            // Crear la orden
                            val order = Order(
                                userId = userId,
                                total = total,
                                status = "CONFIRMED"
                            )
                            val orderId = db.orderDao().insertOrder(order)
                            
                            // Crear los items de la orden
                            cartItems.forEach { cartItem ->
                                val orderItem = OrderItem(
                                    orderId = orderId.toInt(),
                                    productId = cartItem.id,
                                    quantity = cartItem.quantity,
                                    price = cartItem.salePrice ?: cartItem.price
                                )
                                db.orderDao().insertOrderItem(orderItem)
                            }
                            
                            // Limpiar el carrito
                            db.cartDao().clearCart(userId = userId)
                            isProcessing = false
                            showSuccessDialog = true
                        } catch (e: Exception) {
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                enabled = !isProcessing
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_pay_logo),
                        contentDescription = "Google Pay Logo",
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Pay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray,
                    thickness = 1.dp
                )
                Text(
                    text = "O",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray,
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Número de la tarjeta",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { 
                    if (it.length <= 16) cardNumber = it.filter { char -> char.isDigit() }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("1234 5678 9012 3456") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_credit_card_icon),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFFFF9800)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Nombre del titular",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = cardHolder,
                onValueChange = { cardHolder = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Juan Pérez") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_user_icon),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFFFF9800)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Fecha de vencimiento",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { 
                            if (it.length <= 5) {
                                expiryDate = it.filter { char -> char.isDigit() || char == '/' }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("MM/AA") },
                        leadingIcon ={
                            Icon(
                                painter = painterResource(id = R.drawable.ic_date_icon),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color(0xFFFF9800)
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CVV",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { 
                            if (it.length <= 3) cvv = it.filter { char -> char.isDigit() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("123") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_padlock_icon),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color(0xFFFF9800)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isFormValid = cardNumber.length == 16 && 
                              cardHolder.isNotBlank() && 
                              expiryDate.length >= 4 && 
                              cvv.length == 3

            Button(
                onClick = {
                    if (isFormValid) {
                        selectedPaymentMethod = PaymentMethod.CREDIT_CARD
                        finalTotal = total
                        isProcessing = true
                        scope.launch {
                            try {
                                // Crear la orden
                                val order = Order(
                                    userId = userId,
                                    total = total,
                                    status = "CONFIRMED"
                                )
                                val orderId = db.orderDao().insertOrder(order)
                                
                                // Crear los items de la orden
                                cartItems.forEach { cartItem ->
                                    val orderItem = OrderItem(
                                        orderId = orderId.toInt(),
                                        productId = cartItem.id,
                                        quantity = cartItem.quantity,
                                        price = cartItem.salePrice ?: cartItem.price
                                    )
                                    db.orderDao().insertOrderItem(orderItem)
                                }
                                
                                // Limpiar el carrito
                                db.cartDao().clearCart(userId = userId)
                                isProcessing = false
                                showSuccessDialog = true
                            } catch (e: Exception) {
                                isProcessing = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color.Black else Color.Gray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = isFormValid && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Pagar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color.White,
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "¡Pago exitoso!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedPaymentMethod == PaymentMethod.GOOGLE_PAY) {
                            "Tu pago con Google Pay ha sido procesado correctamente."
                        } else {
                            "Tu pago con tarjeta de crédito ha sido procesado correctamente."
                        },
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Total pagado: ${PriceFormatter.formatPrice(finalTotal)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToCart()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        "Continuar comprando",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}