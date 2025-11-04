package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.models.User
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilitar edge-to-edge
        enableEdgeToEdge()
        
        // Hacer que el contenido se dibuje detrás de la barra de estado
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Crear usuario por defecto
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val existingUser = db.userDao().getUserByEmail("default@comicverse.com")
            if (existingUser == null) {
                db.userDao().insertUser(
                    User(
                        id = 1,
                        name = "Usuario Invitado",
                        email = "default@comicverse.com",
                        password = "guest123"
                    )
                )
            }
        }
        
        setContent {
            MyApplicationTheme {
                // Configurar barra de estado
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }
                
                AppNavigation()
            }
        }
    }
}