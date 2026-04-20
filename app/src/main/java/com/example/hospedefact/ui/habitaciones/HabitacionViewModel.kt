package com.example.hospedefact.ui.habitaciones

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.Habitacion
import com.example.hospedefact.data.repository.HabitacionRepository
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel que gestiona la lógica de negocio para las habitaciones del hotel.
 * Facilita la creación, recuperación y eliminación de habitaciones a través del [HabitacionRepository].
 */
class HabitacionViewModel(
    private val repository: HabitacionRepository = HabitacionRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "HabitacionViewModel"
    }

    /**
     * Crea una nueva habitación en el sistema.
     * 
     * @param numero Número identificador de la habitación.
     * @param tipo Categoría de la habitación (ej. "Simple", "Doble", "Suite").
     * @param precioNoche Coste por noche de estancia.
     * @param capacidad Número máximo de huéspedes permitidos.
     * @return [LiveData] que emite el estado de la operación ("exito", "cargando" o mensaje de error).
     */
    fun crearHabitacion(
        numero: Int,
        tipo: String,
        precioNoche: Double,
        capacidad: Int
    ) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando habitacion: $numero")
            emit("cargando")

            val resultado = repository.crearHabitacion(numero, tipo, precioNoche, capacidad)

            resultado.onSuccess { id ->
                Log.d(TAG, "Habitacion creada: $id")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Recupera el listado completo de habitaciones registradas en el hotel.
     * 
     * @return [LiveData] que emite la lista de [Habitacion] o estados de carga/error.
     */
    fun cargarHabitaciones() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando habitaciones")
            emit("cargando")

            val resultado = repository.obtenerHabitaciones()

            resultado.onSuccess { habitaciones ->
                Log.d(TAG, "Se cargaron ${habitaciones.size} habitaciones")
                emit(habitaciones)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Elimina permanentemente una habitación del sistema.
     * 
     * @param habitacionId ID único de la habitación a eliminar.
     * @return [LiveData] con el resultado de la eliminación.
     */
    fun eliminarHabitacion(habitacionId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Eliminando habitacion: $habitacionId")
            emit("cargando")

            val resultado = repository.eliminarHabitacion(habitacionId)

            resultado.onSuccess {
                Log.d(TAG, "Habitacion eliminada")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }
}