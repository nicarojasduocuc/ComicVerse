package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.models.Product
import com.example.myapplication.utils.PriceFormatter
import com.example.myapplication.utils.UserSession
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit = {},
    onProductClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val products by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Verificar si el usuario actual es admin
    val userId = UserSession.getUserId(context)
    var isAdmin by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != -1) {
            val user = db.userDao().getUserById(userId)
            isAdmin = user?.isAdmin == true
        }
    }

    val whiteBackground = Color.White

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(whiteBackground)
                .padding(horizontal = 20.dp)
        ) {
            // 🔹 Header con logo y botones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🖼️ Logo ComicVerse
                Image(
                    painter = painterResource(id = R.drawable.comicverse),
                    contentDescription = "ComicVerse Logo",
                    modifier = Modifier
                        .width(150.dp)
                        .height(40.dp),
                    contentScale = ContentScale.Fit
                )

                // 🔘 Botones con espacio más reducido
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onNavigateToCart) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_filter_icon),
                            contentDescription = "Filtrar",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = { /* TODO: abrir buscador */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_search_icon),
                            contentDescription = "Buscar",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // 🔹 Grid de productos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 120.dp), // ✅ Aumentado de 80dp a 120dp
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        backgroundColor = whiteBackground,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }

        // FloatingActionButton solo visible para admin
        if (isAdmin) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFF9800),
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp, end = 16.dp) // ✅ Aumentado de 100dp a 120dp
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
            }
        }

        // SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp) // ✅ Aumentado de 100dp a 120dp
        )

        // Diálogo para agregar producto
        if (showAddDialog) {
            AddProductDialog(
                onDismiss = { showAddDialog = false },
                onProductAdded = { product ->
                    scope.launch {
                        try {
                            db.productDao().insertProduct(product)
                            showAddDialog = false
                            snackbarHostState.showSnackbar("Producto agregado exitosamente")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error al agregar: ${e.message}")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    backgroundColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClick)
    ) {
        // 🔸 Imagen tipo póster sin bordes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(213.dp)
                .background(backgroundColor)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // 🔸 Badge de descuento
            if (product.salePrice != null && product.salePrice!! < product.price) {
                val discount =
                    ((1 - (product.salePrice!! / product.price)) * 100).toInt().coerceAtLeast(1)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = Color(0xFFFF9800)
                ) {
                    Text(
                        text = "${discount}% DCTO",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Título del producto
        Text(
            text = product.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 🔹 Precio con oferta o normal
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (product.salePrice != null && product.salePrice!! < product.price) {
                Text(
                    text = PriceFormatter.formatPrice(product.salePrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = PriceFormatter.formatPrice(product.price),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textDecoration = TextDecoration.LineThrough
                )
            } else {
                Text(
                    text = PriceFormatter.formatPrice(product.price),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onProductAdded: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var year by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // Lista de tipos disponibles
    val productTypes = listOf(
        "Manga",
        "Comic",
        "Manhwa",
        "Manhua",
        "Novela Gráfica",
        "Webtoon",
        "Light Novel",
        "Artbook",
        "Comic Americano",
        "Comic Europeo"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Dropdown para tipo de producto
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Producto") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        productTypes.forEach { productType ->
                            DropdownMenuItem(
                                text = { Text(productType) },
                                onClick = {
                                    type = productType
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Año") },
                        placeholder = { Text("2024") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock") },
                        placeholder = { Text("10") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio") },
                        placeholder = { Text("12990") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("Precio oferta") },
                        placeholder = { Text("9990") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de la imagen") },
                    placeholder = { Text("https://ejemplo.com/imagen.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                    val salePriceValue = salePrice.toDoubleOrNull()
                    val yearValue = year.toIntOrNull() ?: 2024
                    val stockValue = stock.toIntOrNull() ?: 0

                    val product = Product(
                        name = name,
                        type = type,
                        year = yearValue,
                        stock = stockValue,
                        price = priceValue,
                        salePrice = salePriceValue,
                        description = description,
                        imageUrl = imageUrl.trim()
                    )
                    onProductAdded(product)
                },
                enabled = name.isNotBlank() && type.isNotBlank() && price.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.Black
                )
            ) {
                Text("Agregar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}