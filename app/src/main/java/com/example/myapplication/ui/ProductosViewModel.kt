package com.example.myapplication.ui
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ProductoDao
import com.example.myapplication.data.Producto
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class ProductosViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).productoDao()

    /**
     * Flow observable de Room -> lo convertimos a StateFlow
     * con stateIn para que la UI pueda usar collectAsState de forma simple.
     *
     * SharingStarted.WhileSubscribed(5_000): mantiene el upstream
     * 5s después de que no haya suscriptores (evita re-suscripciones agresivas).
     */
    val productos = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Insertar/actualizar usando @Upsert en un hilo de corrutina */
    fun agregar(nombre: String, descripcion: String, precio: Double) {
        viewModelScope.launch {
            dao.upsert(
                Producto(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    precio = precio
                )
            )
        }
    }

    /** Borrar todo (útil para pruebas) */
    fun limpiar() {
        viewModelScope.launch { dao.clear() }
    }
}
