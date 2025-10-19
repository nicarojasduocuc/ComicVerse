package com.example.myapplication.data
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface ProductoDao {

    // Inserta si no existe (por PK) o actualiza si ya existe (Room 2.6+)
    @Upsert
    suspend fun upsert(producto: Producto)

    // Observa en tiempo real la tabla completa
    @Query("SELECT * FROM productos ORDER BY id DESC")
    fun getAll(): Flow<List<Producto>>

    // Borra todo (útil para pruebas / botón “Limpiar”)
    @Query("DELETE FROM productos")
    suspend fun clear()
}
