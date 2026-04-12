package com.example.hospedefact.data.repository

import android.util.Log
import com.example.hospedefact.data.models.Proveedor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para gestión de Proveedores
 * CRUD: Crear, Leer, Actualizar, Eliminar
 */
class ProveedorRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("proveedores")

    companion object {
        private const val TAG = "ProveedorRepository"
    }

    /**
     * CREAR: Nuevo proveedor
     */
    suspend fun crearProveedor(proveedor: Proveedor): Result<String> = try {
        Log.d(TAG, "Creando proveedor: ${proveedor.nombre}")

        val doc = coleccion.document()
        val proveedorConId = proveedor.copy(id = doc.id)
        doc.set(proveedorConId).await()

        Log.d(TAG, "Proveedor creado: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear proveedor", e)
        Result.failure(e)
    }

    /**
     * LEER: Obtener todos los proveedores activos
     */
    suspend fun obtenerProveedores(): Result<List<Proveedor>> = try {
        Log.d(TAG, "Obteniendo proveedores")

        val snapshot = coleccion
            .whereEqualTo("activo", true)
            .get()
            .await()

        val proveedores = snapshot.toObjects(Proveedor::class.java)
            .sortedBy { it.nombre } // Ordenamos en memoria para evitar el error de índice
        Log.d(TAG, "Se obtuvieron ${proveedores.size} proveedores")
        Result.success(proveedores)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener proveedores", e)
        Result.failure(e)
    }

    /**
     * LEER: Obtener proveedor por ID
     */
    suspend fun obtenerProveedorPorId(proveedorId: String): Result<Proveedor?> = try {
        Log.d(TAG, "Obteniendo proveedor: $proveedorId")

        val doc = coleccion.document(proveedorId).get().await()
        val proveedor = doc.toObject(Proveedor::class.java)

        Result.success(proveedor)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener proveedor", e)
        Result.failure(e)
    }

    /**
     * ACTUALIZAR: Datos de proveedor
     */
    suspend fun actualizarProveedor(proveedor: Proveedor): Result<Unit> = try {
        Log.d(TAG, "Actualizando proveedor: ${proveedor.id}")

        coleccion.document(proveedor.id).set(proveedor).await()

        Log.d(TAG, "Proveedor actualizado")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar proveedor", e)
        Result.failure(e)
    }

    /**
     * DESACTIVAR: Proveedor (baja lógica)
     */
    suspend fun desactivarProveedor(proveedorId: String): Result<Unit> = try {
        Log.d(TAG, "Desactivando proveedor: $proveedorId")

        val proveedorResult = obtenerProveedorPorId(proveedorId)

        if (proveedorResult.isSuccess) {
            val proveedor = proveedorResult.getOrNull()
            if (proveedor != null) {
                val proveedorActualizado = proveedor.copy(
                    activo = false,
                    estado = "inactivo"
                )

                coleccion.document(proveedorId).set(proveedorActualizado).await()
                Log.d(TAG, "Proveedor desactivado")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Proveedor no encontrado"))
            }
        } else {
            Result.failure(proveedorResult.exceptionOrNull() ?: Exception("Error desconocido"))
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al desactivar proveedor", e)
        Result.failure(e)
    }

    /**
     * BUSCAR: Proveedores por nombre
     */
    suspend fun buscarProveedores(nombre: String): Result<List<Proveedor>> = try {
        Log.d(TAG, "Buscando proveedores: $nombre")

        val snapshot = coleccion
            .whereEqualTo("activo", true)
            .get()
            .await()

        val proveedores = snapshot.toObjects(Proveedor::class.java)
        val resultado = proveedores.filter {
            it.nombre.contains(nombre, ignoreCase = true)
        }

        Log.d(TAG, "Se encontraron ${resultado.size} proveedores")
        Result.success(resultado)

    } catch (e: Exception) {
        Log.e(TAG, "Error al buscar proveedores", e)
        Result.failure(e)
    }
}