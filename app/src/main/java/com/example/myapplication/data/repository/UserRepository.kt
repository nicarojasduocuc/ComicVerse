package com.example.myapplication.data.repository

import com.example.myapplication.data.models.User
import com.example.myapplication.data.network.RailwayApiService

/**
 * Repositorio para operaciones con Usuarios usando Railway backend
 */
class UserRepository {
    
    /**
     * Obtiene un usuario por su ID
     */
    suspend fun getUserById(id: Int): Resource<User> {
        return try {
            val user = RailwayApiService.getUserById(id)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener usuario", e)
        }
    }
    
    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val user = RailwayApiService.login(email, password)
            android.util.Log.d("UserRepository", "Login response: $user")
            android.util.Log.d("UserRepository", "User ID: ${user.id}")
            android.util.Log.d("UserRepository", "User email: ${user.email}")
            android.util.Log.d("UserRepository", "User name: ${user.name}")
            Resource.Success(user)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Login error", e)
            Resource.Error(e.message ?: "Email o contraseña incorrectos", e)
        }
    }
    
    /**
     * Registra un nuevo usuario
     */
    suspend fun register(email: String, name: String, password: String): Resource<User> {
        return try {
            val user = RailwayApiService.register(email, name, password)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al registrar usuario", e)
        }
    }
}
