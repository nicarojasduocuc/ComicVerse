package com.example.myapplication.ui
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.myapplication.data.Producto
import java.text.DecimalFormat

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProductos(vm: ProductosViewModel = viewModel()) {
    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioTxt by remember { mutableStateOf("") }

    // Observa el StateFlow del ViewModel de forma lifecycle-aware
    val productos = vm.productos.collectAsStateWithLifecycle()

    val df = remember { DecimalFormat("#,##0.##") }

    Scaffold(
        containerColor = Color.LightGray,
        topBar = { TopAppBar(title = { Text("Productos (Room + Compose)") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") }
            )
            OutlinedTextField(
                value = precioTxt,
                onValueChange = { precioTxt = it },
                label = { Text("Precio (ej: 19990.0)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = {
                    Text("Usa punto decimal. Ejemplos: 999.99 o 1000")
                }
            )

            Button(onClick = {
                val precio = precioTxt.toDoubleOrNull()
                val nombreOk = nombre.isNotBlank()
                val descOk = descripcion.isNotBlank()
                if (nombreOk && descOk && precio != null) {
                    vm.agregar(nombre, descripcion, precio)
                    // Limpiar campos
                    nombre = ""
                    descripcion = ""
                    precioTxt = ""
                }
                // (Opcional) podrías mostrar un Snackbar si hay error de validación
            }) {
                Text("Agregar")
            }

            Divider()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos.value) { p: Producto ->
                    ListItem(
                        headlineContent = {
                            Text("${p.nombre} — $${df.format(p.precio)}")
                        },
                        supportingContent = { Text(p.descripcion) },
                        trailingContent = { Text("#${p.id}") }
                    )
                    Divider()
                }
            }
        }
    }
}
