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

    suspend fun obtenerMesaPorId(mesaId: String): Result<Mesa?> = try {
        Log.d(TAG, "Obteniendo mesa: $mesaId")

        val doc = coleccionMesas.document(mesaId).get().await()
        val mesa = doc.toObject(Mesa::class.java)

        Result.success(mesa)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener mesa", e)
        Result.failure(e)
    }

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