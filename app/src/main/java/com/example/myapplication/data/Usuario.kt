package com.example.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa la tabla "usuarios" en la base de datos.
 * Esta clase almacena la información de un usuario, incluyendo sus credenciales.
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nombre_usuario")
    val nombre: String,

    @ColumnInfo(name = "correo")
    val correo: String,

    @ColumnInfo(name = "contrasena")
    val contrasena: String,

    @ColumnInfo(name = "direcion")
    val direccion: String,

    @ColumnInfo(name = "carrito_id")
    val carritoId: Int,

)

