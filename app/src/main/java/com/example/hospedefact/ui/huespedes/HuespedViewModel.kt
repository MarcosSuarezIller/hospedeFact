package com.example.hospedefact.ui.huespedes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.Huesped
import com.example.hospedefact.data.repository.HuespedRepository
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel para gestión de Huéspedes
 * Comunica entre UI (Fragment) y Repository (Firestore)
 */
class HuespedViewModel(
    private val repository: HuespedRepository = HuespedRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "HuespedViewModel"
    }

    /**
     * Crea un nuevo huésped
     *
     * @param nombre Nombre del huésped
     * @param email Email de contacto
     * @param telefono Teléfono de contacto
     * @param habitacion Número de habitación
     *
     * @return LiveData que emite "exito" o "error: mensaje"
     */
    fun crearHuesped(
        nombre: String,
        email: String,
        telefono: String,
        habitacion: String
    ) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando nuevo huésped: $nombre")

            // Valida que los campos no estén vacíos
            if (nombre.isEmpty() || habitacion.isEmpty()) {
                emit("error: Completa todos los campos")
                return@liveData
            }

            // Crea objeto Huesped
            val huesped = Huesped(
                nombre = nombre,
                email = email,
                telefono = telefono,
                habitacion = habitacion,
                fechaEntrada = System.currentTimeMillis(),
                estado = "activo"
            )

            // Llama al repository
            val resultado = repository.crearHuesped(huesped)

            resultado.onSuccess { id ->
                Log.d(TAG, "Huésped creado con ID: $id")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al crear huésped", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en crearHuesped", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Obtiene lista de todos los huéspedes activos
     *
     * @return LiveData que emite lista o "error: mensaje"
     */
    fun cargarHuespedes() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando lista de huéspedes")
            emit("cargando")

            val resultado = repository.obtenerHuespedes()

            resultado.onSuccess { huespedes ->
                Log.d(TAG, "Se cargaron ${huespedes.size} huéspedes")
                emit(huespedes)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al cargar huéspedes", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en cargarHuespedes", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Obtiene un huésped específico por ID
     *
     * @param huespedId ID del huésped
     * @return LiveData que emite el Huesped o null
     */
    fun obtenerHuespedPorId(huespedId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo huésped: $huespedId")

            val resultado = repository.obtenerHuespedPorId(huespedId)

            resultado.onSuccess { huesped ->
                emit(huesped)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al obtener huésped", exception)
                emit(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en obtenerHuespedPorId", e)
            emit(null)
        }
    }

    /**
     * Actualiza datos de un huésped
     *
     * @param huesped Objeto Huesped con datos actualizados
     * @return LiveData que emite "exito" o "error: mensaje"
     */
    fun actualizarHuesped(huesped: Huesped) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando huésped: ${huesped.id}")
            emit("cargando")

            val resultado = repository.actualizarHuesped(huesped)

            resultado.onSuccess {
                Log.d(TAG, "Huésped actualizado")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al actualizar huésped", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en actualizarHuesped", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Marca un huésped como checkout (baja lógica)
     *
     * @param huespedId ID del huésped
     * @return LiveData que emite "exito" o "error: mensaje"
     */
    fun darDeAltaHuesped(huespedId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Dando de alta huésped: $huespedId")
            emit("cargando")

            val resultado = repository.darDeAltaHuesped(huespedId)

            resultado.onSuccess {
                Log.d(TAG, "Huésped dado de alta")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al dar de alta huésped", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en darDeAltaHuesped", e)
            emit("error: ${e.message}")
        }
    }
}
