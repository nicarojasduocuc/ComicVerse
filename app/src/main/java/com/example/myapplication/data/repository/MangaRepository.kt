package com.example.myapplication.data.repository

import com.example.myapplication.data.models.Manga
import com.example.myapplication.data.network.RailwayApiService

/**
 * Repositorio para operaciones con Mangas usando Railway backend
 */
class MangaRepository {
    
    /**
     * Obtiene todos los mangas desde Railway
     */
    suspend fun getAllMangas(): Resource<List<Manga>> {
        return try {
            val mangas = RailwayApiService.getAllMangas()
            Resource.Success(mangas)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener mangas", e)
        }
    }
    
    /**
     * Obtiene un manga por su ID
     */
    suspend fun getMangaById(id: String): Resource<Manga> {
        return try {
            val manga = RailwayApiService.getMangaById(id)
            Resource.Success(manga)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener manga", e)
        }
    }
    
    /**
     * Busca mangas por nombre
     */
    suspend fun searchMangas(query: String): Resource<List<Manga>> {
        return try {
            val mangas = RailwayApiService.searchMangas(query)
            Resource.Success(mangas)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error en búsqueda", e)
        }
    }
}
