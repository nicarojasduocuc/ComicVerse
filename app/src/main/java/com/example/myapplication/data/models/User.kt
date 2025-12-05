package com.example.myapplication.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de Usuario
 */
@Serializable
data class User(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String? = null,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
