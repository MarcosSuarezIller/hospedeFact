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
     * Inserta un nuevo huésped en la colección de Firestore.
     *
     * @param huesped Objeto [Huesped] que contiene los datos personales y de habitación.
     * @return [Result] con el ID del documento generado en Firestore.
     */
    suspend fun crearHuesped(huesped: Huesped): Result<String> = try {
        Log.d(TAG, "Creando huesped: ${huesped.nombre} con precio noche: ${huesped.precioNocheHabitacion}")

        val doc = coleccion.document()
        coleccion.document(doc.id).set(huesped.copy(id = doc.id)).await()

        Log.d(TAG, "Huesped creado: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear huesped", e)
        Result.failure(e)
    }

    /**
     * Recupera la lista de todos los huéspedes que se encuentran actualmente en estado "activo".
     *
     * @return [Result] que contiene la lista de objetos [Huesped] activos.
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
     * Busca un huésped específico utilizando su identificador único.
     *
     * @param huespedId El ID del documento del huésped en Firestore.
     * @return [Result] con el objeto [Huesped] encontrado o null si no existe.
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
     * Actualiza la información de un huésped ya existente en la base de datos.
     *
     * @param huesped Objeto [Huesped] con los campos actualizados (debe incluir el ID).
     * @return [Result] que indica el éxito o fallo de la actualización.
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
     * Procesa la salida de un huésped, marcándolo con el estado "checkout" y registrando 
     * la fecha actual como fecha de salida (baja lógica).
     *
     * @param huespedId ID único del huésped que realiza el checkout.
     * @return [Result] indicando el éxito del proceso de salida.
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
     * Obtiene una lista completa de todos los huéspedes registrados, independientemente de su estado.
     * Útil para la generación de reportes históricos.
     * 
     * @return [Result] con la lista histórica de huéspedes.
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
    /**
     * Obtiene la información de un huésped junto con los detalles específicos de precio de su habitación.
     * 
     * @param huespedId ID del huésped.
     * @return [Result] que contiene un Par con el objeto [Huesped] y el precio por noche.
     */
    suspend fun obtenerHuespedConDetalles(huespedId: String): Result<Pair<Huesped?, Double>> = try {
        Log.d(TAG, "Obteniendo huesped con detalles: $huespedId")

        val snapshot = coleccion.document(huespedId).get().await()
        val huesped = snapshot.toObject(Huesped::class.java)

        val precioNoche = huesped?.precioNocheHabitacion ?: 0.0

        Result.success(Pair(huesped, precioNoche))

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener huesped", e)
        Result.failure(e)
    }

    /**
     * Realiza el cálculo del costo total de estancia basándose en las fechas de entrada y salida.
     * Asegura un mínimo de una noche cargada.
     * 
     * @param huespedId ID del huésped.
     * @param fechaEntrada Marca de tiempo (ms) de la entrada.
     * @param fechaSalida Marca de tiempo (ms) de la salida.
     * @param precioNoche Costo por noche de la habitación.
     * @return El importe total calculado para la estancia.
     */
    fun calcularCostoEstancia(huespedId: String, fechaEntrada: Long, fechaSalida: Long, precioNoche: Double): Double {
        val diasEstancia = ((fechaSalida - fechaEntrada) / (1000 * 60 * 60 * 24)).toInt()
        return diasEstancia.coerceAtLeast(1) * precioNoche
    }
}

