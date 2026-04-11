package com.example.hospedefact.data.repository

import android.util.Log
import com.example.hospedefact.data.models.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para manejar el Menú en Firestore
 * CRUD: Crear, Leer, Actualizar, Eliminar items del menú
 */
class MenuRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("menu")

    companion object {
        private const val TAG = "MenuRepository"
    }

    /**
     * Obtiene todos los items activos del menú
     * Devuelve lista ordenada por categoría
     */
    suspend fun cargarMenu(): Result<List<MenuItem>> = try {
        Log.d(TAG, "Cargando menú")

        val snapshot = coleccion
            .whereEqualTo("activo", true)
            .get()
            .await()

        val items = snapshot.toObjects(MenuItem::class.java)
            .sortedBy { it.categoria }
        Log.d(TAG, "Menú cargado: ${items.size} items")
        Result.success(items)

    } catch (e: Exception) {
        Log.e(TAG, "Error al cargar menú", e)
        Result.failure(e)
    }

    /**
     * Obtiene items de una categoría específica
     */
    suspend fun cargarMenuPorCategoria(categoria: String): Result<List<MenuItem>> = try {
        Log.d(TAG, "Cargando menú de categoría: $categoria")

        val snapshot = coleccion
            .whereEqualTo("categoria", categoria)
            .whereEqualTo("activo", true)
            .get()
            .await()

        val items = snapshot.toObjects(MenuItem::class.java)
        Log.d(TAG, "Se obtuvieron ${items.size} items de $categoria")
        Result.success(items)

    } catch (e: Exception) {
        Log.e(TAG, "Error al cargar menú por categoría", e)
        Result.failure(e)
    }

    /**
     * Crea un nuevo item en el menú
     */
    suspend fun crearMenuItem(item: MenuItem): Result<String> = try {
        Log.d(TAG, "Creando item: ${item.nombre}")

        val doc = coleccion.document()
        val itemConId = item.copy(id = doc.id)
        doc.set(itemConId).await()

        Log.d(TAG, "Item creado: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear item", e)
        Result.failure(e)
    }

    /**
     * Actualiza un item del menú
     */
    suspend fun actualizarMenuItem(item: MenuItem): Result<Unit> = try {
        Log.d(TAG, "Actualizando item: ${item.id}")

        coleccion.document(item.id).set(item).await()

        Log.d(TAG, "Item actualizado")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar item", e)
        Result.failure(e)
    }

    /**
     * Desactiva un item del menú (baja lógica)
     */
    suspend fun desactivarMenuItem(itemId: String): Result<Unit> = try {
        Log.d(TAG, "Desactivando item: $itemId")

        coleccion.document(itemId).update("activo", false).await()

        Log.d(TAG, "Item desactivado")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al desactivar item", e)
        Result.failure(e)
    }

    /**
     * Obtiene todas las categorías disponibles
     */
    suspend fun obtenerCategorias(): Result<List<String>> = try {
        Log.d(TAG, "Obteniendo categorías")

        val snapshot = coleccion
            .whereEqualTo("activo", true)
            .get()
            .await()

        val items = snapshot.toObjects(MenuItem::class.java)
        val categorias = items.map { it.categoria }.distinct().sorted()

        Log.d(TAG, "Categorías obtenidas: $categorias")
        Result.success(categorias)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener categorías", e)
        Result.failure(e)
    }
}
