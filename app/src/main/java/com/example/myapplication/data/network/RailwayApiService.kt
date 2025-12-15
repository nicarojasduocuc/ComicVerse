package com.example.myapplication.data.network

import com.example.myapplication.data.models.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Request para login
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Request para registro
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String
)

/**
 * Servicio API que consume los endpoints de Railway usando Ktor
 */
object RailwayApiService {
    
    private val client = RailwayApiClient.httpClient
    
    // ==================== MANGAS ====================
    
    suspend fun getAllMangas(): List<Manga> {
        return client.get("/api/mangas").body()
    }
    
    suspend fun getMangaById(id: String): Manga {
        return client.get("/api/mangas/$id").body()
    }
    
    suspend fun searchMangas(query: String): List<Manga> {
        return client.get("/api/mangas/search") {
            parameter("q", query)
        }.body()
    }
    
    // ==================== USUARIOS ====================
    
    suspend fun getUserById(id: Int): User {
        return client.get("/api/users/$id").body()
    }
    
    suspend fun login(email: String, password: String): User {
        return try {
            val response = client.post("/api/users/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            val body = response.body<String>()
            android.util.Log.d("RailwayApiService", "Login response status: ${response.status}")
            android.util.Log.d("RailwayApiService", "Login response body: $body")
            
            // Intentar parsear manualmente
            val json = kotlinx.serialization.json.Json { 
                ignoreUnknownKeys = true
                isLenient = true
            }
            json.decodeFromString<User>(body)
        } catch (e: Exception) {
            android.util.Log.e("RailwayApiService", "Login error: ${e.message}", e)
            throw e
        }
    }
    
    suspend fun register(email: String, name: String, password: String): User {
        return try {
            val response = client.post("/api/users") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, name, password))
            }
            android.util.Log.d("RailwayApiService", "Register response status: ${response.status}")
            
            if (response.status.value in 200..299) {
                response.body<User>()
            } else {
                val errorBody = response.body<String>()
                android.util.Log.e("RailwayApiService", "Register failed: $errorBody")
                throw Exception("Registration failed: $errorBody")
            }
        } catch (e: Exception) {
            android.util.Log.e("RailwayApiService", "Register error: ${e.message}", e)
            throw e
        }
    }
    
    suspend fun updateUser(id: Int, user: User): User {
        return client.put("/api/users/$id") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }
    
    // ==================== ÓRDENES ====================
    
    suspend fun getOrdersByUserId(userId: Int): List<Order> {
        return client.get("/api/orders/user/$userId").body()
    }
    
    suspend fun getOrderById(id: Int): Order {
        return client.get("/api/orders/$id").body()
    }
    
    suspend fun getOrderItems(orderId: Int): List<OrderItem> {
        return client.get("/api/orders/$orderId/items").body()
    }
    
    suspend fun createOrder(request: CreateOrderRequest): Order {
        return client.post("/api/orders") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateOrderStatus(orderId: Int, status: String): Order {
        return client.put("/api/orders/$orderId/status") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status))
        }.body()
    }
    
    // ==================== MERCADO PAGO ====================
    
    /**
     * Crea una preferencia de pago en Mercado Pago
     */
    suspend fun createPayment(request: PaymentRequest): PaymentResponse {
        return try {
            val response = client.post("/api/payments/create") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            android.util.Log.d("RailwayApiService", "Payment response status: ${response.status}")
            
            if (response.status.value in 200..299) {
                response.body<PaymentResponse>()
            } else {
                val errorBody = response.body<String>()
                android.util.Log.e("RailwayApiService", "Payment failed: $errorBody")
                throw Exception("Payment creation failed: $errorBody")
            }
        } catch (e: Exception) {
            android.util.Log.e("RailwayApiService", "Payment error: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Obtiene información de un pago por su ID
     */
    suspend fun getPaymentInfo(paymentId: String): PaymentInfo {
        return client.get("/api/payments/$paymentId").body()
    }
    
    /**
     * Verifica el estado de un pago
     */
    suspend fun verifyPaymentStatus(paymentId: String): String {
        val paymentInfo = getPaymentInfo(paymentId)
        return paymentInfo.status
    }
    
    /**
     * Procesa la orden después de un pago exitoso
     */
    suspend fun processOrder(externalReference: String): Boolean {
        return try {
            val response = client.post("/api/payments/process-order") {
                parameter("externalReference", externalReference)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            android.util.Log.e("RailwayApiService", "Process order error: ${e.message}", e)
            false
        }
    }
}
