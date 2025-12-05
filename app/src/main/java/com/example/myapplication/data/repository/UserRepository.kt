package com.example.myapplication.data.repository

import com.example.myapplication.data.models.User
import com.example.myapplication.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from

/**
 * Repositorio para operaciones con Usuarios usando Supabase directamente
 */
class UserRepository {
    
    private val supabase = SupabaseClient.client
    
    /**
     * Obtiene un usuario por su ID
     */
    suspend fun getUserById(id: Int): Resource<User> {
        return try {
            val user = supabase.from("users")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<User>()
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
            // Primero buscar por email
            val users = supabase.from("users")
                .select()
                .decodeList<User>()
            
            // Filtrar localmente para debug
            val matchingUser = users.find { 
                it.email.equals(email.trim(), ignoreCase = true) && 
                it.password == password.trim()
            }
            
            if (matchingUser != null) {
                Resource.Success(matchingUser)
            } else {
                Resource.Error("Email o contraseña incorrectos. Usuarios encontrados: ${users.size}")
            }
        } catch (e: Exception) {
            Resource.Error("Error al iniciar sesión: ${e.message}", e)
        }
    }
    
    /**
     * Registra un nuevo usuario
     */
    suspend fun register(email: String, name: String, password: String): Resource<User> {
        return try {
            // Verificar si el email ya existe
            val existingUsers = supabase.from("users")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<User>()
            
            if (existingUsers.isNotEmpty()) {
                return Resource.Error("El email ya está registrado")
            }
            
            // Insertar nuevo usuario y obtener el resultado
            val response = supabase.from("users")
                .insert(
                    mapOf(
                        "email" to email,
                        "name" to name,
                        "password" to password // En producción, usa hashing
                    )
                ) {
                    select()
                }
                .decodeSingle<User>()
            
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al registrar usuario", e)
        }
    }
}
