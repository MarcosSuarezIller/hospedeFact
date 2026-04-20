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
     * Registra un nuevo huésped en el sistema a través del repositorio.
     * Valida que los campos obligatorios (nombre y habitación) no estén vacíos.
     *
     * @param huesped El objeto [Huesped] con la información a persistir.
     * @return [LiveData] que emite el estado del proceso: "exito", "cargando" o un mensaje de "error:".
     */
    fun crearHuesped(huesped: Huesped) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando nuevo huésped: ${huesped.nombre}")

            // Valida que los campos no estén vacíos
            if (huesped.nombre.isEmpty() || huesped.habitacion.isEmpty()) {
                emit("error: Completa todos los campos")
                return@liveData
            }

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
     * Método de conveniencia para crear un huésped a partir de campos individuales.
     * 
     * @param nombre Nombre completo del huésped.
     * @param email Correo electrónico de contacto.
     * @param telefono Número de teléfono.
     * @param habitacion Identificador o nombre de la habitación asignada.
     * @return [LiveData] con el resultado de la operación de creación.
     */
    fun crearHuesped(
        nombre: String,
        email: String,
        telefono: String,
        habitacion: String
    ) = crearHuesped(
        Huesped(
            nombre = nombre,
            email = email,
            telefono = telefono,
            habitacion = habitacion,
            fechaEntrada = System.currentTimeMillis(),
            estado = "activo"
        )
    )

    /**
     * Inicia la carga de la lista de huéspedes activos desde Firestore.
     * 
     * @return [LiveData] que emite el estado "cargando", la lista de [Huesped] o un mensaje de error.
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
     * Recupera la información de un huésped específico por su identificador.
     *
     * @param huespedId ID único del huésped en el sistema.
     * @return [LiveData] que emite el objeto [Huesped] encontrado o null en caso de error.
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
     * Envía una solicitud de actualización de los datos de un huésped existente.
     *
     * @param huesped Objeto [Huesped] que contiene el ID y los nuevos valores.
     * @return [LiveData] que notifica el resultado ("exito" o "error:").
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
     * Procesa la baja (checkout) de un huésped del hotel.
     *
     * @param huespedId ID único del huésped que abandona el establecimiento.
     * @return [LiveData] que emite el resultado de la transacción.
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
