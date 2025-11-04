package com.example.myapplication.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String = "",
    val imageUrl: String = "",
    val year: Int = 0,
    val type: String = "",
    val stock: Int = 0,
    val salePrice: Double? = null
)