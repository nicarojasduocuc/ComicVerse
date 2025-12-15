package com.example.myapplication.data.models

import kotlinx.serialization.Serializable

/**
 * Request para crear un pago con Mercado Pago
 */
@Serializable
data class PaymentRequest(
    val title: String,
    val description: String? = null,
    val price: String, // String para evitar problemas de serialización
    val quantity: Int = 1,
    val currencyId: String = "CLP",
    val externalReference: String? = null,
    val payerEmail: String? = null,
    val userId: Int? = null, // ID del usuario para crear la orden
    val items: List<CartItemForPayment>? = null // Items del carrito
)

@Serializable
data class CartItemForPayment(
    val manga_id: String,
    val quantity: Int
)

/**
 * Response con la información de la preferencia de pago creada
 */
@Serializable
data class PaymentResponse(
    val id: String,
    val initPoint: String,
    val sandboxInitPoint: String? = null
)

/**
 * Información detallada de un pago
 */
@Serializable
data class PaymentInfo(
    val id: Long,
    val status: String,
    val statusDetail: String? = null,
    val transactionAmount: String, // Como String para evitar problemas de serialización
    val currencyId: String,
    val dateCreated: String? = null,
    val dateApproved: String? = null,
    val externalReference: String? = null,
    val payerEmail: String? = null
)
