package com.example.myapplication.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myapplication.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val window = (context as Activity).window
    val useDarkIcons = !isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background

    SideEffect {
        window.statusBarColor = backgroundColor.toArgb()
        window.navigationBarColor = backgroundColor.toArgb()

        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = useDarkIcons
        controller.isAppearanceLightNavigationBars = useDarkIcons
    }

    LaunchedEffect(Unit) {
        delay(2000) // 2 segundos
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.comicverse),
                contentDescription = "ComicVerse Logo",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 24.dp)
            )

            CircularProgressIndicator(
                strokeWidth = 3.dp,
                color = Color(0xFFFF9800),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}