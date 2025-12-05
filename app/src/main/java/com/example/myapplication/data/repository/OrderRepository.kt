package com.example.myapplication.data.repository

import com.example.myapplication.data.models.Order
import com.example.myapplication.data.models.OrderItem
import com.example.myapplication.data.models.CreateOrderRequest
import com.example.myapplication.data.models.CreateOrderItemRequest
import com.example.myapplication.data.network.RailwayApiService

/**
 * Repositorio para operaciones con Órdenes usando Railway backend
 */
class OrderRepository {
    
    /**
     * Obtiene todas las órdenes de un usuario
     */
    suspend fun getOrdersByUserId(userId: Int): Resource<List<Order>> {
        return try {
            val orders = RailwayApiService.getOrdersByUserId(userId)
            Resource.Success(orders)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener órdenes", e)
        }
    }
    
    /**
     * Obtiene una orden por su ID
     */
    suspend fun getOrderById(id: Int): Resource<Order> {
        return try {
            val order = RailwayApiService.getOrderById(id)
            Resource.Success(order)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener orden", e)
        }
    }
    
    /**
     * Obtiene los items de una orden
     */
    suspend fun getOrderItems(orderId: Int): Resource<List<OrderItem>> {
        return try {
            val items = RailwayApiService.getOrderItems(orderId)
            Resource.Success(items)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener items", e)
        }
    }
    
    /**
     * Crea una nueva orden
     */
    suspend fun createOrder(userId: Int, items: List<CreateOrderItemRequest>): Resource<Order> {
        return try {
            val request = CreateOrderRequest(userId = userId, items = items)
            val order = RailwayApiService.createOrder(request)
            Resource.Success(order)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al crear orden", e)
        }
    }
}
