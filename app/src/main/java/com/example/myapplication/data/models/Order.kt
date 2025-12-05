package com.example.myapplication.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de Orden
 */
@Serializable
data class Order(
    @SerialName("id") val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("total") val total: Int,
    @SerialName("status") val status: String = "PENDING",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Item de una orden
 */
@Serializable
data class OrderItem(
    @SerialName("id") val id: Int,
    @SerialName("order_id") val orderId: Int,
    @SerialName("manga_id") val mangaId: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price") val price: Int
)

/**
 * Request para crear una orden
 */
@Serializable
data class CreateOrderRequest(
    @SerialName("user_id") val userId: Int,
    val items: List<CreateOrderItemRequest>
)

/**
 * Request para crear un item de orden
 */
@Serializable
data class CreateOrderItemRequest(
    @SerialName("manga_id") val mangaId: String,
    val quantity: Int
)

/**
 * Request para actualizar el estado de una orden
 */
data class UpdateOrderRequest(
    val status: String
)

/**
 * Orden con detalles completos (incluye items y datos de mangas)
 */
@Serializable
data class OrderWithDetails(
    @SerialName("id") val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("user_name") val userName: String,
    @SerialName("total") val total: Int,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    val items: List<OrderItemWithManga>
)

/**
 * Item de orden con información del manga
 */
@Serializable
data class OrderItemWithManga(
    @SerialName("id") val id: Int,
    @SerialName("manga_id") val mangaId: String,
    @SerialName("manga_name") val mangaName: String?,
    @SerialName("manga_poster") val mangaPoster: String?,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price") val price: Int
)

/**
 * Estados posibles de una orden
 */
enum class OrderStatus(val value: String) {
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    SHIPPED("SHIPPED"),
    DELIVERED("DELIVERED"),
    CANCELLED("CANCELLED")
}
