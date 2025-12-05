package com.example.myapplication.data.repository

/**
 * Clase sellada para manejar estados de operaciones asíncronas
 */
sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val exception: Exception? = null) : Resource<T>()
}
