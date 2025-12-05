package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.models.Manga
import com.example.myapplication.data.models.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para la gestión del carrito de compras
 */
class CartViewModel : ViewModel() {
    
    // Estado del carrito
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    // Total del carrito
    private val _cartTotal = MutableStateFlow(0)
    val cartTotal: StateFlow<Int> = _cartTotal.asStateFlow()
    
    /**
     * Agrega un manga al carrito
     */
    fun addToCart(manga: Manga, quantity: Int = 1) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.manga.id == manga.id }
        
        if (existingItem != null) {
            // Si ya existe, actualiza la cantidad
            val index = currentItems.indexOf(existingItem)
            currentItems[index] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            // Si no existe, agrégalo
            currentItems.add(CartItem(manga, quantity))
        }
        
        _cartItems.value = currentItems
        calculateTotal()
    }
    
    /**
     * Actualiza la cantidad de un item
     */
    fun updateQuantity(mangaId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(mangaId)
            return
        }
        
        val currentItems = _cartItems.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.manga.id == mangaId }
        
        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(quantity = quantity)
            _cartItems.value = currentItems
            calculateTotal()
        }
    }
    
    /**
     * Elimina un item del carrito
     */
    fun removeFromCart(mangaId: String) {
        _cartItems.value = _cartItems.value.filter { it.manga.id != mangaId }
        calculateTotal()
    }
    
    /**
     * Limpia todo el carrito
     */
    fun clearCart() {
        _cartItems.value = emptyList()
        _cartTotal.value = 0
    }
    
    /**
     * Calcula el total del carrito
     */
    private fun calculateTotal() {
        val total = _cartItems.value.sumOf { item ->
            val price = item.manga.salePrice ?: item.manga.price
            price * item.quantity
        }
        _cartTotal.value = total
    }
    
    /**
     * Obtiene la cantidad total de items en el carrito
     */
    fun getCartItemCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }
}
