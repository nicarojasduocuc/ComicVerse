package com.example.myapplication.data.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


object RailwayApiClient {
    const val BASE_URL = "https://comicverse-backend-production.up.railway.app"
    
    val httpClient = HttpClient(Android) {
        // Configuración de timeout
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
        
        // Logging para debug
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        
        // Serialización JSON
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
        
        // Configuración por defecto
        defaultRequest {
            url(BASE_URL)
        }
    }
}
