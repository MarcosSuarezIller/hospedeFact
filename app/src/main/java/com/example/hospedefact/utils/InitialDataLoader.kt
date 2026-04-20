package com.example.hospedefact.utils

import android.util.Log
import com.example.hospedefact.data.models.MenuItem
import com.example.hospedefact.data.repository.MenuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Objeto utilitario encargado de poblar la base de datos de Firestore con datos iniciales.
 * Se utiliza principalmente durante la primera ejecución para garantizar que el sistema
 * cuente con un catálogo base de productos y servicios.
 */
object InitialDataLoader {

    private const val TAG = "InitialDataLoader"
    private val menuRepository = MenuRepository()

    /**
     * Inicia la carga del menú básico de restauración (bebidas, comidas y postres).
     * El método verifica primero si ya existen datos para evitar duplicidades y ejecuta
     * la persistencia en un hilo secundario mediante [Dispatchers.IO].
     */
    fun cargarMenuInicial() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Obtiene menú actual
                val menuActual = menuRepository.cargarMenu().getOrNull() ?: emptyList()

                // Si hay menú, no crea más
                if (menuActual.isNotEmpty()) {
                    Log.d(TAG, "Menú ya existe")
                    return@launch
                }

                Log.d(TAG, "Creando menú inicial...")

                val items = listOf(
                    MenuItem(nombre = "Agua", descripcion = "Agua mineral", precio = 2.5, categoria = "bebida"),
                    MenuItem(nombre = "Cerveza", descripcion = "Cerveza artesanal", precio = 4.5, categoria = "bebida"),
                    MenuItem(nombre = "Café", descripcion = "Café espresso", precio = 2.0, categoria = "bebida"),
                    MenuItem(nombre = "Refresco", descripcion = "Refresco de cola", precio = 3.0, categoria = "bebida"),
                    MenuItem(nombre = "Hamburguesa", descripcion = "Hamburguesa completa con queso", precio = 12.0, categoria = "comida"),
                    MenuItem(nombre = "Ensalada", descripcion = "Ensalada mixta fresca", precio = 8.5, categoria = "comida"),
                    MenuItem(nombre = "Pizza", descripcion = "Pizza margarita", precio = 14.0, categoria = "comida"),
                    MenuItem(nombre = "Pasta", descripcion = "Pasta a la carbonara", precio = 11.0, categoria = "comida"),
                    MenuItem(nombre = "Helado", descripcion = "Helado de vainilla", precio = 5.0, categoria = "postre"),
                    MenuItem(nombre = "Tiramisú", descripcion = "Postre italiano", precio = 6.5, categoria = "postre"),
                    MenuItem(nombre = "Chocolate", descripcion = "Postre de chocolate", precio = 5.5, categoria = "postre")
                )

                for (item in items) {
                    menuRepository.crearMenuItem(item)
                    Log.d(TAG, "Item creado: ${item.nombre}")
                }

                Log.d(TAG, "Menú inicial cargado completamente")

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar menú inicial", e)
            }
        }
    }
}
