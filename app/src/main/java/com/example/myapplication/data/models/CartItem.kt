package com.example.myapplication.data.models

/**
 * Item del carrito de compras (solo estado local, no en Supabase)
 */
data class CartItem(
    val manga: Manga,
    val quantity: Int
)
