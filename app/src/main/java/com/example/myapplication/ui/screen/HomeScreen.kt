package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit = {},
    onProductClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val products by db.productDao().getAllProducts().collectAsState(initial = emptyList())

    val whiteBackground = Color.White

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
            contentPadding = PaddingValues(bottom = 80.dp),
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