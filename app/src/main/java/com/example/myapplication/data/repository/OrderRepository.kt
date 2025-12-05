package com.example.myapplication.data.repository

import com.example.myapplication.data.models.Order
import com.example.myapplication.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class OrderItem(
    val id: Int? = null,
    val order_id: Int,
    val manga_id: String,
    val quantity: Int,
    val price: Int
)

@Serializable
data class CreateOrderItem(
    val manga_id: String,
    val quantity: Int,
    val price: Int
)

@Serializable
data class InsertOrderItem(
    val order_id: Int,
    val manga_id: String,
    val quantity: Int,
    val price: Int
)

/**
 * Repositorio para operaciones con Órdenes usando Supabase directamente
 */
class OrderRepository {
    
    private val supabase = SupabaseClient.client
    
    /**
     * Obtiene todas las órdenes de un usuario
     */
    suspend fun getOrdersByUserId(userId: Int): Resource<List<Order>> {
        return try {
            val orders = supabase.from("orders")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Order>()
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
            val order = supabase.from("orders")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Order>()
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
            val items = supabase.from("order_items")
                .select {
                    filter {
                        eq("order_id", orderId)
                    }
                }
                .decodeList<OrderItem>()
            Resource.Success(items)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener items", e)
        }
    }
    
    /**
     * Crea una nueva orden
     */
    suspend fun createOrder(userId: Int, total: Int, items: List<CreateOrderItem>): Resource<Order> {
        return try {
            // Crear la orden usando JSON explícito
            val orderJson = buildJsonObject {
                put("user_id", userId)
                put("total", total)
                put("status", "PENDING")
            }
            
            val order = supabase.from("orders")
                .insert(orderJson) {
                    select()
                }
                .decodeSingle<Order>()
            
            // Insertar los items de la orden
            items.forEach { item ->
                val itemJson = buildJsonObject {
                    put("order_id", order.id)
                    put("manga_id", item.manga_id)
                    put("quantity", item.quantity)
                    put("price", item.price)
                }
                
                supabase.from("order_items")
                    .insert(itemJson)
            }
            
            Resource.Success(order)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al crear orden: ${e.message}", e)
        }
    }
}
