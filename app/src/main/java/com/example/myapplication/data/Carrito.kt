package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Representa un artículo en el carrito de compras.
 * Esta clase almacena la información de un producto agregado al carrito por un usuario.
 */
@Entity(tableName = "carrito")

data class Carrito(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "usuario_id", index = true)
    val usuarioId: Int,
    @ColumnInfo(name = "producto_id", index = true)
    val productoId: Int,
    val cantidad: Int
)
