package com.example.hospedefact.data.repository

import android.util.Log
import com.example.hospedefact.data.models.Huesped
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para manejar operaciones de Huéspedes en Firestore
 * CRUD: Crear, Leer, Actualizar, Eliminar
 */
class HuespedRepository {

    // Instancia de Firestore
    private val db = FirebaseFirestore.getInstance()

    // Referencia a la colección "huespedes"
    private val coleccion = db.collection("huespedes")

    companion object {
        private const val TAG = "HuespedRepository"
    }

    /**
     * CREAR: Inserta un nuevo huésped en Firestore
     *
     * @param huesped Objeto Huesped a guardar
     * @return Result.success(id) si funciona, Result.failure(exception) si falla
     */
    suspend fun crearHuesped(huesped: Huesped): Result<String> = try {
        Log.d(TAG, "Creando nuevo huesped: ${huesped.nombre}")

        // Crea un documento con ID automático
        val doc = coleccion.document()

        // Copia el huesped con el ID generado
        val huespedConId = huesped.copy(id = doc.id)

        // Guarda en Firestore
        doc.set(huespedConId).await()

        Log.d(TAG, "Huesped creado exitosamente. ID: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear huesped", e)
        Result.failure(e)
    }

    /**
     * LEER: Obtiene todos los huéspedes activos
     *
     * @return Result.success(lista) si funciona, Result.failure(exception) si falla
     */
    suspend fun obtenerHuespedes(): Result<List<Huesped>> = try {
        Log.d(TAG, "Obteniendo lista de huéspedes activos")

        // Query a Firestore: obtener solo huéspedes con estado "activo"
        val snapshot = coleccion
            .whereEqualTo("estado", "activo")
            .get()
            .await()

        // Convierte los documentos a objetos Huesped
        val huespedes = snapshot.toObjects(Huesped::class.java)

        Log.d(TAG, "Se obtuvieron ${huespedes.size} huéspedes")
        Result.success(huespedes)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener huéspedes", e)
        Result.failure(e)
    }

    /**
     * LEER: Obtiene un huésped específico por ID
     *
     * @param huespedId ID del huésped a obtener
     * @return Result.success(huesped) si funciona, Result.failure(exception) si falla
     */
    suspend fun obtenerHuespedPorId(huespedId: String): Result<Huesped?> = try {
        Log.d(TAG, "Obteniendo huésped con ID: $huespedId")

        // Obtiene el documento
        val doc = coleccion.document(huespedId).get().await()

        // Convierte a objeto Huesped
        val huesped = doc.toObject(Huesped::class.java)

        if (huesped != null) {
            Log.d(TAG, "Huésped encontrado: ${huesped.nombre}")
            Result.success(huesped)
        } else {
            Log.w(TAG, "Huésped no encontrado")
            Result.success(null)
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener huésped por ID", e)
        Result.failure(e)
    }

    /**
     * ACTUALIZAR: Actualiza los datos de un huésped
     *
     * @param huesped Objeto Huesped con datos actualizados
     * @return Result.success(Unit) si funciona, Result.failure(exception) si falla
     */
    suspend fun actualizarHuesped(huesped: Huesped): Result<Unit> = try {
        Log.d(TAG, "Actualizando huésped: ${huesped.id}")

        // Actualiza el documento en Firestore
        coleccion.document(huesped.id).set(huesped).await()

        Log.d(TAG, "Huésped actualizado exitosamente")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar huésped", e)
        Result.failure(e)
    }

    /**
     * ELIMINAR: Marca un huésped como "checkout" (baja lógica)
     * No elimina físicamente, solo marca como inactivo
     *
     * @param huespedId ID del huésped a "eliminar"
     * @return Result.success(Unit) si funciona, Result.failure(exception) si falla
     */
    suspend fun darDeAltaHuesped(huespedId: String): Result<Unit> = try {
        Log.d(TAG, "Marcando huésped como checkout: $huespedId")

        // Obtiene el huésped
        val huespedResult = obtenerHuespedPorId(huespedId)

        if (huespedResult.isSuccess) {
            val huesped = huespedResult.getOrNull()
            if (huesped != null) {
                // Marca como "checkout"
                val huespedActualizado = huesped.copy(
                    estado = "checkout",
                    fechaSalida = System.currentTimeMillis()
                )

                // Guarda
                coleccion.document(huespedId).set(huespedActualizado).await()

                Log.d(TAG, "Huésped marcado como checkout")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Huésped no encontrado"))
            }
        } else {
            Result.failure(huespedResult.exceptionOrNull() ?: Exception("Error desconocido"))
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al dar de alta huésped", e)
        Result.failure(e)
    }

    /**
     * Obtiene todos los huéspedes (incluyendo inactivos)
     * Útil para reportes
     */
    suspend fun obtenerTodosHuespedes(): Result<List<Huesped>> = try {
        Log.d(TAG, "Obteniendo TODOS los huéspedes")

        val snapshot = coleccion.get().await()
        val huespedes = snapshot.toObjects(Huesped::class.java)

        Log.d(TAG, "Total de huéspedes: ${huespedes.size}")
        Result.success(huespedes)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener todos los huéspedes", e)
        Result.failure(e)
    }
}

