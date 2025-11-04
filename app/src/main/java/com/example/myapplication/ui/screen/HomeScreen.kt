package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.myapplication.db.models.CartItem
import com.example.myapplication.db.models.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val products by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.comicverse),
                            contentDescription = "Logo ComicVerse",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ComicVerse", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800),
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFF9800),
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onAddToCart = {
                        scope.launch {
                            try {
                                val userId = 1
                                
                                val existingItem = db.cartDao().getCartItem(userId, product.id)
                                if (existingItem != null) {
                                    db.cartDao().updateCartItem(
                                        existingItem.copy(quantity = existingItem.quantity + 1)
                                    )
                                } else {
                                    db.cartDao().insertCartItem(
                                        CartItem(userId = userId, productId = product.id, quantity = 1)
                                    )
                                }
                                
                                // Mostrar mensaje breve
                                snackbarHostState.showSnackbar(
                                    message = "✓ Agregado al carrito",
                                    duration = SnackbarDuration.Short
                                )
                                
                                // Pequeño delay para que el usuario vea el mensaje
                                delay(300)
                                
                                // Navegar al carrito
                                onNavigateToCart()
                                
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onProductAdded = { product ->
                scope.launch {
                    try {
                        db.productDao().insertProduct(product)
                        showAddDialog = false
                        snackbarHostState.showSnackbar("Producto agregado")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error al agregar: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    var isLoading by remember { mutableStateOf(true) }
                    var hasError by remember { mutableStateOf(false) }

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            isLoading = true
                            hasError = false
                        },
                        onSuccess = {
                            isLoading = false
                            hasError = false
                        },
                        onError = {
                            isLoading = false
                            hasError = true
                        }
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color(0xFFFF9800)
                        )
                    }

                    if (hasError) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Error al cargar",
                                modifier = Modifier.size(50.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "Error al cargar",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = "Sin imagen",
                            modifier = Modifier.size(50.dp),
                            tint = Color.Gray
                        )
                        Text("Sin imagen", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Agregar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onProductAdded: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Producto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de la imagen") },
                    placeholder = { Text("https://ejemplo.com/imagen.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tip: Usa URLs directas de imágenes como imgur.com, i.ibb.co, etc.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceValue = price.toDoubleOrNull() ?: 0.0
                    val product = Product(
                        name = name,
                        price = priceValue,
                        description = description,
                        imageUrl = imageUrl.trim()
                    )
                    onProductAdded(product)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.Black
                )
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}