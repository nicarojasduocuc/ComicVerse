package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.UserSession
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Guardar el deep link si viene de un pago
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
                
                AppNavigation()
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        android.util.Log.d("MainActivity", "onNewIntent con data: ${intent.data}")
        handleDeepLink(intent)
        recreate() // Reiniciar la activity para que se aplique el deep link
    }
    
    override fun onResume() {
        super.onResume()
        android.util.Log.d("MainActivity", "onResume - verificando deep link")
        // También verificar en onResume por si acaso
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        android.util.Log.d("MainActivity", "handleDeepLink llamado con data: $data")
        
        if (data != null && data.scheme == "comicverse" && data.host == "payment") {
            android.util.Log.d("MainActivity", "Deep link de pago detectado - path: ${data.path}")
            
            // Marcar que ya no estamos procesando el pago
            UserSession.setIsProcessingPayment(this, false)
            
            when (data.path) {
                "/success" -> {
                    android.util.Log.d("MainActivity", "Pago exitoso - navegando a Orders")
                    UserSession.setNeedsNavigateToOrders(this, true)
                }
                "/failure" -> {
                    android.util.Log.d("MainActivity", "Pago fallido")
                    // Podrías mostrar un mensaje de error aquí
                }
                "/pending" -> {
                    android.util.Log.d("MainActivity", "Pago pendiente")
                    // Podrías mostrar un mensaje de pendiente aquí
                }
            }
        }
    }
}