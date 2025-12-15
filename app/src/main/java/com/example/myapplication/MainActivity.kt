package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    private var deepLinkData = mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manejar Deep Link
        handleDeepLink(intent)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MyApplicationTheme {
                val systemUiController = rememberSystemUiController()
                
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = true
                    )
                }
                
                AppNavigation(deepLinkRoute = deepLinkData.value)
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && data.scheme == "comicverse" && data.host == "payment") {
            // Redirigir a Orders cuando viene de un pago
            deepLinkData.value = "orders"
        }
    }
}