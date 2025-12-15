package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.repository.Resource
import com.example.myapplication.utils.PriceFormatter
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.OrdersViewModel
import com.example.myapplication.utils.UserSession
import com.example.myapplication.data.models.PaymentRequest
import com.example.myapplication.data.models.CartItemForPayment
import com.example.myapplication.data.network.RailwayApiService
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val ordersViewModel: OrdersViewModel = viewModel()
    
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartTotal by cartViewModel.cartTotal.collectAsState()
    val createOrderState by ordersViewModel.createOrderState.collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isProcessing by remember { mutableStateOf(false) }
    
    // Campos del formulario
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Tarjeta de Crédito") }
    
    val subtotal = cartTotal
    val envio = if (subtotal > 0) 3000 else 0
    val total = subtotal + envio


    LaunchedEffect(createOrderState) {
        when (createOrderState) {
            is Resource.Success -> {
                isProcessing = false
                cartViewModel.clearCart()
                snackbarHostState.showSnackbar("¡Pedido realizado con éxito!")
                onNavigateToOrders()
            }
            is Resource.Error -> {
                isProcessing = false
                snackbarHostState.showSnackbar(
                    (createOrderState as Resource.Error).message
                )
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Finalizar Compra",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    SectionHeader(
                        icon = Icons.Default.LocalShipping,
                        title = "Información de Envío"
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CustomTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = "Nombre Completo",
                                icon = Icons.Default.Person
                            )
                            
                            CustomTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = "Teléfono",
                                icon = Icons.Default.Phone,
                                keyboardType = KeyboardType.Phone
                            )
                            
                            CustomTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = "Dirección",
                                icon = Icons.Default.Home
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CustomTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = "Ciudad",
                                    icon = Icons.Default.LocationCity,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                CustomTextField(
                                    value = postalCode,
                                    onValueChange = { postalCode = it },
                                    label = "Código Postal",
                                    icon = Icons.Default.MarkunreadMailbox,
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                

                item {
                    SectionHeader(
                        icon = Icons.Default.Payment,
                        title = "Método de Pago"
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMethodOption(
                                icon = Icons.Default.CreditCard,
                                title = "Mercado Pago",
                                subtitle = "Tarjetas, efectivo y más",
                                isSelected = selectedPaymentMethod == "Mercado Pago",
                                onClick = { selectedPaymentMethod = "Mercado Pago" }
                            )
                            
                            Divider()
                            
                            PaymentMethodOption(
                                icon = Icons.Default.AccountBalance,
                                title = "Transferencia Bancaria",
                                subtitle = "Pago directo desde tu banco",
                                isSelected = selectedPaymentMethod == "Transferencia Bancaria",
                                onClick = { selectedPaymentMethod = "Transferencia Bancaria" }
                            )
                            
                            Divider()
                            
                            PaymentMethodOption(
                                icon = Icons.Default.Money,
                                title = "Efectivo",
                                subtitle = "Pago al momento de entrega",
                                isSelected = selectedPaymentMethod == "Efectivo",
                                onClick = { selectedPaymentMethod = "Efectivo" }
                            )
                        }
                    }
                }
                

                item {
                    SectionHeader(
                        icon = Icons.Default.ShoppingCart,
                        title = "Resumen de Compra"
                    )
                }
                
                items(cartItems) { item ->
                    CheckoutItemCard(item.manga.name, item.quantity, item.manga.salePrice ?: item.manga.price)
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:", color = Color.Gray)
                                Text(PriceFormatter.formatPrice(subtotal))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Envío:", color = Color.Gray)
                                Text(PriceFormatter.formatPrice(envio))
                            }
                            Divider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total:",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    PriceFormatter.formatPrice(total),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }
            }
            
            // Botón de confirmar
            Button(
                onClick = {
                    if (!isProcessing) {
                        // Validar campos
                        if (fullName.isBlank() || phone.isBlank() || address.isBlank() || 
                            city.isBlank() || postalCode.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor, completa todos los campos")
                            }
                            return@Button
                        }
                        
                        isProcessing = true
                        
                        // Si el método de pago es Mercado Pago, redirigir al checkout
                        if (selectedPaymentMethod == "Mercado Pago") {
                            scope.launch {
                                try {
                                    // Obtener el userId de la sesión
                                    val userId = UserSession.getUserId(context)
                                    
                                    // Crear lista de items para el pago
                                    val paymentItems = cartItems.map { cartItem ->
                                        CartItemForPayment(
                                            manga_id = cartItem.manga.id ?: "",
                                            quantity = cartItem.quantity
                                        )
                                    }
                                    
                                    // Crear el request de pago
                                    val externalRef = "ORDER-${System.currentTimeMillis()}"
                                    val paymentRequest = PaymentRequest(
                                        title = "Compra en ComicVerse",
                                        description = "Compra de ${cartItems.size} productos",
                                        price = total.toString(),
                                        quantity = 1,
                                        currencyId = "CLP",
                                        externalReference = externalRef,
                                        payerEmail = UserSession.getUserEmail(context),
                                        userId = userId,
                                        items = paymentItems
                                    )
                                    
                                    // Guardar la referencia para procesar después
                                    UserSession.savePendingOrderReference(context, externalRef)
                                    
                                    // Marcar que se está haciendo un pago
                                    UserSession.setIsProcessingPayment(context, true)
                                    
                                    // Llamar a la API para crear la preferencia de pago
                                    val paymentResponse = RailwayApiService.createPayment(paymentRequest)
                                    
                                    // Abrir el navegador INMEDIATAMENTE
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentResponse.initPoint))
                                    context.startActivity(intent)
                                    
                                    // Después navegar a Home (no al carrito para no mostrar carrito vacío)
                                    onNavigateToHome()
                                    
                                    // Limpiar el carrito después de navegar
                                    cartViewModel.clearCart()
                                    
                                    isProcessing = false
                                } catch (e: Exception) {
                                    isProcessing = false
                                    snackbarHostState.showSnackbar(
                                        "Error al procesar el pago: ${e.message}"
                                    )
                                    android.util.Log.e("CheckoutScreen", "Error creating payment", e)
                                }
                            }
                        } else {
                            // Otros métodos de pago: crear orden directamente
                            val userId = UserSession.getUserId(context) ?: 1
                            scope.launch {
                                ordersViewModel.createOrder(userId, cartItems)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                enabled = !isProcessing && cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Confirmar Pedido",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(24.dp)
        )
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFF9800)
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFF9800),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedLabelColor = Color(0xFFFF9800)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
fun PaymentMethodOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFFFF9800),
                unselectedColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFFFF9800) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else Color.Gray
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CheckoutItemCard(name: String, quantity: Int, price: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${quantity}x",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }
                
                Text(
                    name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
            }
            
            Text(
                PriceFormatter.formatPrice(price * quantity),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
        }
    }
}
