package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.repository.Resource
import com.example.myapplication.utils.UserSession
import com.example.myapplication.viewmodel.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val userViewModel: UserViewModel = viewModel()
    val loginState by userViewModel.loginState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observar cambios en el estado de login
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is Resource.Success -> {
                val user = state.data
                if (user != null) {
                    // Guardar sesión
                    com.example.myapplication.utils.UserSession.saveUserId(context, user.id)
                    isLoading = false
                    onNavigateToHome()
                }
            }
            is Resource.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(
                    (loginState as Resource.Error).message
                )
            }
            is Resource.Loading -> {
                isLoading = true
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "ComicVerse",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Inicia sesión para continuar",
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        scope.launch {
                            userViewModel.login(email, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "¿No tienes cuenta? Regístrate",
                    color = Color.Black
                )
            }
        }
    }
}
