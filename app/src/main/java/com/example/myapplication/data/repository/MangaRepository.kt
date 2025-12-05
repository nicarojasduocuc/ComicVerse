package com.example.myapplication.data.repository

import com.example.myapplication.data.models.Manga
import com.example.myapplication.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from

/**
 * Repositorio para operaciones con Mangas usando Supabase directamente
 */
class MangaRepository {
    
    private val supabase = SupabaseClient.client
    
    /**
     * Obtiene todos los mangas de Supabase
     */
    suspend fun getAllMangas(): Resource<List<Manga>> {
        return try {
            val mangas = supabase.from("mangas")
                .select()
                .decodeList<Manga>()
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
            val manga = supabase.from("mangas")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<Manga>()
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
            val mangas = supabase.from("mangas")
                .select {
                    filter {
                        ilike("name", "%$query%")
                    }
                }
                .decodeList<Manga>()
            Resource.Success(mangas)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error en búsqueda", e)
        }
    }
}
