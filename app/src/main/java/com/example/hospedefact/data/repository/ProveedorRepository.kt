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
     * Registra un nuevo proveedor en Firestore.
     * 
     * @param proveedor Objeto [Proveedor] con la información de contacto y fiscal.
     * @return [Result] con el ID del documento creado o error.
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
     * Obtiene la lista de todos los proveedores que están marcados como activos.
     * Realiza un ordenamiento por nombre en memoria para evitar la necesidad de índices complejos en Firestore.
     * 
     * @return [Result] con la lista de [Proveedor] encontrados.
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
     * Recupera un proveedor específico mediante su identificador único.
     * 
     * @param proveedorId ID del proveedor a buscar.
     * @return [Result] con el objeto [Proveedor] si existe, o null.
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
     * Actualiza la información de un proveedor existente.
     * 
     * @param proveedor Objeto [Proveedor] con los datos actualizados e ID válido.
     * @return [Result] indicando el éxito o fallo.
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
     * Realiza una baja lógica desactivando a un proveedor y cambiando su estado a "inactivo".
     * 
     * @param proveedorId ID del proveedor a desactivar.
     * @return [Result] que indica el éxito o fallo de la operación.
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
     * Filtra los proveedores activos cuyo nombre contenga la cadena especificada.
     * La búsqueda no distingue entre mayúsculas y minúsculas.
     * 
     * @param nombre Cadena de texto a buscar en el nombre del proveedor.
     * @return [Result] con la lista de proveedores que coinciden con el filtro.
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