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

enum class SortOption {
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    NAME_A_TO_Z,
    NAME_Z_TO_A,
    YEAR_NEW_TO_OLD,
    YEAR_OLD_TO_NEW,
    NONE
}

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit = {},
    onProductClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados de filtros
    var showFilterDialog by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.NONE) }
    var priceRange by remember { mutableStateOf(0f..50000f) }
    var selectedTypes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // Verificar si el usuario actual es admin
    val userId = UserSession.getUserId(context)
    val isLoggedIn = UserSession.isLoggedIn(context)
    var isAdmin by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Obtener favoritos si está logueado
    val favoriteProducts by if (isLoggedIn && userId > 0) {
        db.favoriteDao().getFavoritesByUser(userId).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    LaunchedEffect(userId) {
        if (userId != -1) {
            val user = db.userDao().getUserById(userId)
            isAdmin = user?.isAdmin == true
        }
    }

    // Aplicar filtros
    val filteredProducts = remember(allProducts, sortOption, priceRange, selectedTypes, showOnlyFavorites, favoriteProducts) {
        var filtered = allProducts

        // Filtrar por tipos seleccionados
        if (selectedTypes.isNotEmpty()) {
            filtered = filtered.filter { it.type in selectedTypes }
        }

        // Filtrar por rango de precio
        filtered = filtered.filter { 
            val price = it.salePrice ?: it.price
            price in priceRange.start.toDouble()..priceRange.endInclusive.toDouble()
        }

        // Filtrar por favoritos
        if (showOnlyFavorites && isLoggedIn) {
            val favoriteIds = favoriteProducts.map { it.productId }.toSet()
            filtered = filtered.filter { it.id in favoriteIds }
        }

        // Ordenar según la opción seleccionada
        when (sortOption) {
            SortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.salePrice ?: it.price }
            SortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.salePrice ?: it.price }
            SortOption.NAME_A_TO_Z -> filtered.sortedBy { it.name }
            SortOption.NAME_Z_TO_A -> filtered.sortedByDescending { it.name }
            SortOption.YEAR_NEW_TO_OLD -> filtered.sortedByDescending { it.year }
            SortOption.YEAR_OLD_TO_NEW -> filtered.sortedBy { it.year }
            SortOption.NONE -> filtered
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

                // 🔘 Botones
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Botón de filtro
                    IconButton(onClick = { showFilterDialog = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_filter_icon),
                            contentDescription = "Filtrar",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    // Botón de búsqueda (placeholder)
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
                contentPadding = PaddingValues(bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
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
                    .padding(bottom = 120.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
            }
        }

        // SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )

        // Diálogo de filtros
        if (showFilterDialog) {
            FilterDialog(
                currentSortOption = sortOption,
                currentPriceRange = priceRange,
                currentSelectedTypes = selectedTypes,
                currentShowOnlyFavorites = showOnlyFavorites,
                isLoggedIn = isLoggedIn,
                allProducts = allProducts,
                onDismiss = { showFilterDialog = false },
                onApplyFilters = { newSort, newPriceRange, newTypes, newShowFavorites ->
                    sortOption = newSort
                    priceRange = newPriceRange
                    selectedTypes = newTypes
                    showOnlyFavorites = newShowFavorites
                    showFilterDialog = false
                },
                onClearFilters = {
                    sortOption = SortOption.NONE
                    priceRange = 0f..50000f
                    selectedTypes = emptySet()
                    showOnlyFavorites = false
                }
            )
        }

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
fun FilterDialog(
    currentSortOption: SortOption,
    currentPriceRange: ClosedFloatingPointRange<Float>,
    currentSelectedTypes: Set<String>,
    currentShowOnlyFavorites: Boolean,
    isLoggedIn: Boolean,
    allProducts: List<Product>,
    onDismiss: () -> Unit,
    onApplyFilters: (SortOption, ClosedFloatingPointRange<Float>, Set<String>, Boolean) -> Unit,
    onClearFilters: () -> Unit
) {
    var sortOption by remember { mutableStateOf(currentSortOption) }
    var priceRange by remember { mutableStateOf(currentPriceRange) }
    var selectedTypes by remember { mutableStateOf(currentSelectedTypes) }
    var showOnlyFavorites by remember { mutableStateOf(currentShowOnlyFavorites) }

    // Obtener tipos únicos de productos
    val availableTypes = remember(allProducts) {
        allProducts.map { it.type }.distinct().sorted()
    }

    // Calcular rango de precios de productos
    val minPrice = remember(allProducts) {
        allProducts.minOfOrNull { it.salePrice ?: it.price }?.toFloat() ?: 0f
    }
    val maxPrice = remember(allProducts) {
        allProducts.maxOfOrNull { it.salePrice ?: it.price }?.toFloat() ?: 50000f
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtros", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                TextButton(onClick = {
                    onClearFilters()
                    sortOption = SortOption.NONE
                    priceRange = minPrice..maxPrice
                    selectedTypes = emptySet()
                    showOnlyFavorites = false
                }) {
                    Text("Limpiar", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Ordenar por
                Text(
                    "Ordenar por",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SortOptionItem("Precio: Menor a Mayor", sortOption == SortOption.PRICE_LOW_TO_HIGH) {
                        sortOption = SortOption.PRICE_LOW_TO_HIGH
                    }
                    SortOptionItem("Precio: Mayor a Menor", sortOption == SortOption.PRICE_HIGH_TO_LOW) {
                        sortOption = SortOption.PRICE_HIGH_TO_LOW
                    }
                    SortOptionItem("Nombre: A - Z", sortOption == SortOption.NAME_A_TO_Z) {
                        sortOption = SortOption.NAME_A_TO_Z
                    }
                    SortOptionItem("Nombre: Z - A", sortOption == SortOption.NAME_Z_TO_A) {
                        sortOption = SortOption.NAME_Z_TO_A
                    }
                    SortOptionItem("Año: Más reciente", sortOption == SortOption.YEAR_NEW_TO_OLD) {
                        sortOption = SortOption.YEAR_NEW_TO_OLD
                    }
                    SortOptionItem("Año: Más antiguo", sortOption == SortOption.YEAR_OLD_TO_NEW) {
                        sortOption = SortOption.YEAR_OLD_TO_NEW
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))

                // Rango de precio
                Text(
                    "Rango de precio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "${PriceFormatter.formatPrice(priceRange.start.toDouble())} - ${PriceFormatter.formatPrice(priceRange.endInclusive.toDouble())}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RangeSlider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    valueRange = minPrice..maxPrice,
                    steps = 20,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF9800),
                        activeTrackColor = Color(0xFFFF9800),
                        inactiveTrackColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))

                // Filtrar por tipo
                Text(
                    "Tipo de producto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                availableTypes.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTypes = if (type in selectedTypes) {
                                    selectedTypes - type
                                } else {
                                    selectedTypes + type
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = type in selectedTypes,
                            onCheckedChange = {
                                selectedTypes = if (it) selectedTypes + type else selectedTypes - type
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF9800),
                                uncheckedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type, fontSize = 14.sp)
                    }
                }

                // Mostrar solo favoritos (si está logueado)
                if (isLoggedIn) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOnlyFavorites = !showOnlyFavorites }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showOnlyFavorites,
                            onCheckedChange = { showOnlyFavorites = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF9800),
                                uncheckedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mostrar solo favoritos", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyFilters(sortOption, priceRange, selectedTypes, showOnlyFavorites)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.Black
                )
            ) {
                Text("Aplicar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SortOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) Color(0xFFFFF3E0) else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 12.dp, horizontal = 12.dp),
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
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isSelected) Color.Black else Color.DarkGray
        )
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