package com.example.hospedefact.data.repository

import android.util.Log
import com.example.hospedefact.data.models.Mesa
import com.example.hospedefact.data.models.HistorialMesa
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MesaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccionMesas = db.collection("mesas")
    private val coleccionHistorial = db.collection("historial_mesas")

    companion object {
        private const val TAG = "MesaRepository"
    }

    /**
     * Obtiene la lista de todas las mesas marcadas como activas en el sistema.
     * 
     * @return [Result] con la lista de objetos [Mesa] ordenadas por número.
     */
    suspend fun obtenerMesas(): Result<List<Mesa>> = try {
        Log.d(TAG, "Obteniendo mesas")

        val snapshot = coleccionMesas
            .whereEqualTo("activo", true)
            .get()
            .await()

        val mesas = snapshot.toObjects(Mesa::class.java).sortedBy { it.numero }
        Log.d(TAG, "Se obtuvieron ${mesas.size} mesas")
        Result.success(mesas)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener mesas", e)
        Result.failure(e)
    }

    /**
     * Obtiene los detalles de una mesa específica mediante su ID.
     * 
     * @param mesaId ID único de la mesa.
     * @return [Result] con el objeto [Mesa] si existe, o null.
     */
    suspend fun obtenerMesaPorId(mesaId: String): Result<Mesa?> = try {
        Log.d(TAG, "Obteniendo mesa: $mesaId")

        val doc = coleccionMesas.document(mesaId).get().await()
        val mesa = doc.toObject(Mesa::class.java)

        Result.success(mesa)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener mesa", e)
        Result.failure(e)
    }

    /**
     * Registra una nueva mesa en la base de datos.
     * 
     * @param numero Número identificador de la mesa.
     * @param capacidad Cantidad de comensales.
     * @param ubicacion Descripción de dónde se encuentra la mesa (ej. Terraza, Interior).
     * @return [Result] con el ID de la nueva mesa creada.
     */
    suspend fun crearMesa(numero: Int, capacidad: Int, ubicacion: String): Result<String> = try {
        Log.d(TAG, "Creando mesa: $numero")

        val mesa = Mesa(
            numero = numero,
            capacidad = capacidad,
            ubicacion = ubicacion,
            estado = "disponible"
        )

        val doc = coleccionMesas.document()
        coleccionMesas.document(doc.id).set(mesa.copy(id = doc.id)).await()

        Log.d(TAG, "Mesa creada: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear mesa", e)
        Result.failure(e)
    }

    /**
     * Realiza una baja lógica de la mesa y registra el evento en el historial.
     * 
     * @param mesaId ID de la mesa a eliminar.
     * @return [Result] indicando éxito o fallo de la operación.
     */
    suspend fun eliminarMesa(mesaId: String): Result<Unit> = try {
        Log.d(TAG, "Eliminando mesa: $mesaId")

        val mesaResult = obtenerMesaPorId(mesaId)

        if (mesaResult.isSuccess) {
            val mesa = mesaResult.getOrNull()
            if (mesa != null) {
                val mesaActualizada = mesa.copy(activo = false)
                coleccionMesas.document(mesaId).set(mesaActualizada).await()

                registrarHistorial(mesaId, "eliminada", mesa.huespedId)

                Log.d(TAG, "Mesa eliminada")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Mesa no encontrada"))
            }
        } else {
            Result.failure(mesaResult.exceptionOrNull() ?: Exception("Error desconocido"))
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al eliminar mesa", e)
        Result.failure(e)
    }

    /**
     * Cambia el estado de una mesa a "ocupada" y la vincula con un huésped y pedido.
     * 
     * @param mesaId ID de la mesa.
     * @param huespedId ID del huésped que ocupa la mesa.
     * @param pedidoId ID del pedido asociado a la mesa.
     * @return [Result] indicando éxito o fallo.
     */
    suspend fun ocuparMesa(mesaId: String, huespedId: String, pedidoId: String): Result<Unit> = try {
        Log.d(TAG, "Ocupando mesa: $mesaId para huesped: $huespedId")

        val mesaResult = obtenerMesaPorId(mesaId)

        if (mesaResult.isSuccess) {
            val mesa = mesaResult.getOrNull()
            if (mesa != null) {
                val mesaActualizada = mesa.copy(
                    estado = "ocupada",
                    huespedId = huespedId,
                    pedidoId = pedidoId,
                    fechaOcupacion = System.currentTimeMillis()
                )

                coleccionMesas.document(mesaId).set(mesaActualizada).await()
                registrarHistorial(mesaId, "ocupada", huespedId)

                Log.d(TAG, "Mesa ocupada")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Mesa no encontrada"))
            }
        } else {
            Result.failure(mesaResult.exceptionOrNull() ?: Exception("Error desconocido"))
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al ocupar mesa", e)
        Result.failure(e)
    }

    /**
     * Cambia el estado de una mesa a "disponible" y limpia las referencias a huéspedes o pedidos.
     * 
     * @param mesaId ID de la mesa a liberar.
     * @return [Result] indicando éxito o fallo.
     */
    suspend fun liberarMesa(mesaId: String): Result<Unit> = try {
        Log.d(TAG, "Liberando mesa: $mesaId")

        val mesaResult = obtenerMesaPorId(mesaId)

        if (mesaResult.isSuccess) {
            val mesa = mesaResult.getOrNull()
            if (mesa != null) {
                val mesaActualizada = mesa.copy(
                    estado = "disponible",
                    huespedId = null,
                    pedidoId = null,
                    fechaOcupacion = 0
                )

                coleccionMesas.document(mesaId).set(mesaActualizada).await()
                registrarHistorial(mesaId, "liberada", mesa.huespedId)

                Log.d(TAG, "Mesa liberada")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Mesa no encontrada"))
            }
        } else {
            Result.failure(mesaResult.exceptionOrNull() ?: Exception("Error desconocido"))
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al liberar mesa", e)
        Result.failure(e)
    }

    /**
     * Obtiene una lista de todas las mesas que están actualmente disponibles.
     * 
     * @return [Result] con la lista de mesas disponibles ordenadas.
     */
    suspend fun obtenerMesasDisponibles(): Result<List<Mesa>> = try {
        Log.d(TAG, "Obteniendo mesas disponibles")

        val snapshot = coleccionMesas
            .whereEqualTo("estado", "disponible")
            .whereEqualTo("activo", true)
            .get()
            .await()

        val mesas = snapshot.toObjects(Mesa::class.java).sortedBy { it.numero }
        Log.d(TAG, "Mesas disponibles: ${mesas.size}")
        Result.success(mesas)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener mesas disponibles", e)
        Result.failure(e)
    }

    /**
     * Obtiene una lista de todas las mesas que están actualmente ocupadas.
     * 
     * @return [Result] con la lista de mesas ocupadas ordenadas.
     */
    suspend fun obtenerMesasOcupadas(): Result<List<Mesa>> = try {
        Log.d(TAG, "Obteniendo mesas ocupadas")

        val snapshot = coleccionMesas
            .whereEqualTo("estado", "ocupada")
            .whereEqualTo("activo", true)
            .get()
            .await()

        val mesas = snapshot.toObjects(Mesa::class.java).sortedBy { it.numero }
        Log.d(TAG, "Mesas ocupadas: ${mesas.size}")
        Result.success(mesas)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener mesas ocupadas", e)
        Result.failure(e)
    }

    /**
     * Registra una acción relacionada con una mesa en la colección de historial.
     * 
     * @param mesaId ID de la mesa.
     * @param accion Descripción de la acción (ej. "ocupada", "liberada").
     * @param huespedId ID opcional del huésped involucrado.
     * @param notas Notas adicionales sobre el evento.
     */
    private suspend fun registrarHistorial(
        mesaId: String,
        accion: String,
        huespedId: String? = null,
        notas: String = ""
    ) {
        try {
            Log.d(TAG, "Registrando historial: $mesaId - $accion")

            val historial = HistorialMesa(
                mesaId = mesaId,
                accion = accion,
                huespedId = huespedId,
                notas = notas
            )

            val doc = coleccionHistorial.document()
            coleccionHistorial.document(doc.id).set(historial.copy(id = doc.id)).await()

            Log.d(TAG, "Historial registrado")

        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar historial", e)
        }
    }
}