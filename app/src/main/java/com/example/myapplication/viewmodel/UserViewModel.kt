package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.User
import com.example.myapplication.data.repository.Resource
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de usuarios (Login, Registro, Cuenta)
 */
class UserViewModel : ViewModel() {
    
    private val repository = UserRepository()
    
    // Estado de login
    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState.asStateFlow()
    
    // Estado de registro
    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState.asStateFlow()
    
    // Estado de usuario actual
    private val _currentUser = MutableStateFlow<Resource<User>?>(null)
    val currentUser: StateFlow<Resource<User>?> = _currentUser.asStateFlow()
    
    /**
     * Inicia sesión con email y contraseña
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _loginState.value = repository.login(email, password)
        }
    }
    
    /**
     * Registra un nuevo usuario
     */
    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            _registerState.value = repository.register(email, name, password)
        }
    }
    
    /**
     * Obtiene la información del usuario actual
     */
    fun loadUserProfile(userId: Int) {
        viewModelScope.launch {
            _currentUser.value = Resource.Loading()
            _currentUser.value = repository.getUserById(userId)
        }
    }
    
    /**
     * Resetea el estado de login
     */
    fun resetLoginState() {
        _loginState.value = null
    }
    
    /**
     * Resetea el estado de registro
     */
    fun resetRegisterState() {
        _registerState.value = null
    }
    
    /**
     * Cierra sesión (limpia los estados)
     */
    fun logout() {
        _loginState.value = null
        _currentUser.value = null
    }
}
