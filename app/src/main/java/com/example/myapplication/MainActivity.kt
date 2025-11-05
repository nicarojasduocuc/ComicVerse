package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        
        enableEdgeToEdge()
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val existingUser = db.userDao().getUserByEmail("comicverse@gmail.com")
            if (existingUser == null) {
                db.userDao().insertUser(
                    User(
                        id = 1,
                        name = "Administrador",
                        email = "comicverse@gmail.com",
                        password = "comicverse",
                        isAdmin = true
                    )
                )
            }
        }
        
        setContent {
            MyApplicationTheme {
                val systemUiController = rememberSystemUiController()
                
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = true
                    )
                }
                
                AppNavigation()
            }
        }
    }
}