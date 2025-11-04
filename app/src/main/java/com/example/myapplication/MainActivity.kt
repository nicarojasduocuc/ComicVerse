package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.models.User
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                AppNavigation()
            }
        }
    }
}