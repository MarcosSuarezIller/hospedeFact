package com.example.hospedefact.data.repository

import android.util.Log
import com.example.hospedefact.data.models.Habitacion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HabitacionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccionHabitaciones = db.collection("habitaciones")

    companion object {
        private const val TAG = "HabitacionRepository"
    }

    /**
     * Crea una nueva habitación en la colección de Firestore.
     * 
     * @param numero El número identificador de la habitación.
     * @param tipo El tipo de habitación (ej. "Simple", "Doble").
     * @param precioNoche El costo de la estancia por noche.
     * @param capacidad La cantidad máxima de personas permitidas.
     * @return Un objeto [Result] con el ID del documento creado o una excepción en caso de fallo.
     */
    suspend fun crearHabitacion(
        numero: Int,
        tipo: String,
        precioNoche: Double,
        capacidad: Int
    ): Result<String> = try {
        Log.d(TAG, "Creando habitacion: $numero")

        val habitacion = Habitacion(
            numero = numero,
            tipo = tipo,
            precioNoche = precioNoche,
            capacidad = capacidad,
            estado = "disponible"
        )

        val doc = coleccionHabitaciones.document()
        coleccionHabitaciones.document(doc.id).set(habitacion.copy(id = doc.id)).await()

        Log.d(TAG, "Habitacion creada: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear habitacion", e)
        Result.failure(e)
    }

    /**
     * Obtiene la lista de todas las habitaciones marcadas como activas.
     * 
     * @return Un objeto [Result] que contiene la lista de habitaciones encontradas.
     */
    suspend fun obtenerHabitaciones(): Result<List<Habitacion>> = try {
        Log.d(TAG, "Obteniendo habitaciones")

        val snapshot = coleccionHabitaciones
            .orderBy("numero")
            .get()
            .await()

        val habitaciones = snapshot.toObjects(Habitacion::class.java).filter { it.activo }
        Log.d(TAG, "Se obtuvieron ${habitaciones.size} habitaciones")
        Result.success(habitaciones)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener habitaciones", e)
        Result.failure(e)
    }

    /**
     * Realiza una baja lógica de una habitación marcándola como inactiva.
     * 
     * @param habitacionId El ID único de la habitación a eliminar.
     * @return Un objeto [Result] indicando el éxito o fallo de la operación.
     */
    suspend fun eliminarHabitacion(habitacionId: String): Result<Unit> = try {
        Log.d(TAG, "Eliminando habitacion: $habitacionId")

        val doc = coleccionHabitaciones.document(habitacionId).get().await()
        val habitacion = doc.toObject(Habitacion::class.java)

        if (habitacion != null) {
            coleccionHabitaciones.document(habitacionId)
                .set(habitacion.copy(activo = false)).await()

            Log.d(TAG, "Habitacion eliminada")
            Result.success(Unit)
        } else {
            Result.failure(Exception("Habitacion no encontrada"))
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al eliminar habitacion", e)
        Result.failure(e)
    }
}