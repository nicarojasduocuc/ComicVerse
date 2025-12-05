package com.example.myapplication.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de Manga/Comic (coincide exactamente con tabla Supabase)
 */
@Serializable
data class Manga(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String? = null,
    @SerialName("year") val year: Int? = null,
    @SerialName("stock") val stock: Int = 0,
    @SerialName("price") val price: Int,
    @SerialName("sale_price") val salePrice: Int? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("poster") val poster: String? = null
)
