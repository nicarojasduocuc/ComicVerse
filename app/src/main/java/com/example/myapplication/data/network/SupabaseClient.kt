package com.example.myapplication.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    private const val SUPABASE_URL = "https://uurdnaxmjuhgxwzuowzw.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV1cmRuYXhtanVoZ3h3enVvd3p3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ2MDMyOTYsImV4cCI6MjA4MDE3OTI5Nn0.dkHSjAR97U8iVPsEJor32DhW6YrUmhAwC7JD-v4HVho"
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }
}
