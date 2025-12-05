package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.Order
import com.example.myapplication.data.models.CartItem
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.Resource
import com.example.myapplication.data.repository.CreateOrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de órdenes
 */
class OrdersViewModel : ViewModel() {
    
    private val repository = OrderRepository()
    
    // Estado de las órdenes del usuario
    private val _orders = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val orders: StateFlow<Resource<List<Order>>> = _orders.asStateFlow()
    
    // Estado de una orden específica
    private val _orderDetails = MutableStateFlow<Resource<Order>?>(null)
    val orderDetails: StateFlow<Resource<Order>?> = _orderDetails.asStateFlow()
    
    // Estado de creación de orden
    private val _createOrderState = MutableStateFlow<Resource<Order>?>(null)
    val createOrderState: StateFlow<Resource<Order>?> = _createOrderState.asStateFlow()
    
    /**
     * Obtiene todas las órdenes de un usuario
     */
    fun getUserOrders(userId: Int) {
        viewModelScope.launch {
            _orders.value = Resource.Loading()
            _orders.value = repository.getOrdersByUserId(userId)
        }
    }
    
    /**
     * Obtiene los detalles de una orden específica
     */
    fun getOrderDetails(orderId: Int) {
        viewModelScope.launch {
            _orderDetails.value = Resource.Loading()
            _orderDetails.value = repository.getOrderById(orderId)
        }
    }
    
    /**
     * Crea una nueva orden desde el carrito
     */
    fun createOrder(userId: Int, cartItems: List<CartItem>, total: Int) {
        viewModelScope.launch {
            _createOrderState.value = Resource.Loading()
            
            val orderItems = cartItems.map { cartItem ->
                val price = cartItem.manga.salePrice ?: cartItem.manga.price
                CreateOrderItem(
                    manga_id = cartItem.manga.id,
                    quantity = cartItem.quantity,
                    price = price
                )
            }
            
            _createOrderState.value = repository.createOrder(userId, total, orderItems)
        }
    }
    
    /**
     * Resetea el estado de creación de orden
     */
    fun resetCreateOrderState() {
        _createOrderState.value = null
    }
}
