package com.example.myapplication.data

data class Product(
    val id: Int = 0,
    val name: String,
    val price: Double,
    val imageUrl: String = "",
    val description: String = ""
)