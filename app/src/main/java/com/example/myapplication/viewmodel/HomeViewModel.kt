package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.models.Manga
import com.example.myapplication.data.repository.MangaRepository
import com.example.myapplication.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Mangas (pantalla Home)
 */
class HomeViewModel : ViewModel() {
    
    private val repository = MangaRepository()
    
    // Estado de la lista de mangas
    private val _mangas = MutableStateFlow<Resource<List<Manga>>>(Resource.Loading())
    val mangas: StateFlow<Resource<List<Manga>>> = _mangas.asStateFlow()
    
    // Estado de un manga específico
    private val _mangaDetails = MutableStateFlow<Resource<Manga>?>(null)
    val mangaDetails: StateFlow<Resource<Manga>?> = _mangaDetails.asStateFlow()
    
    // Estado de búsqueda
    private val _searchResults = MutableStateFlow<Resource<List<Manga>>?>(null)
    val searchResults: StateFlow<Resource<List<Manga>>?> = _searchResults.asStateFlow()
    
    init {
        loadMangas()
    }
    
    /**
     * Carga todos los mangas disponibles
     */
    fun loadMangas() {
        viewModelScope.launch {
            _mangas.value = Resource.Loading()
            _mangas.value = repository.getAllMangas()
        }
    }
    
    /**
     * Obtiene los detalles de un manga específico
     */
    suspend fun getMangaDetails(mangaId: String): Resource<Manga> {
        return repository.getMangaById(mangaId)
    }
    
    /**
     * Busca mangas por nombre
     */
    fun searchMangas(query: String) {
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }
        
        viewModelScope.launch {
            _searchResults.value = Resource.Loading()
            _searchResults.value = repository.searchMangas(query)
        }
    }
    
    /**
     * Limpia los resultados de búsqueda
     */
    fun clearSearch() {
        _searchResults.value = null
    }
    
    /**
     * Recarga los mangas (para pull-to-refresh)
     */
    fun refresh() {
        loadMangas()
    }
}
