package com.example.hospedefact.ui.restauracion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.Mesa
import com.example.hospedefact.data.repository.MesaRepository
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel que gestiona la lógica de negocio para las mesas del restaurante.
 * Proporciona métodos para consultar disponibilidad, crear/eliminar mesas y gestionar sus estados (ocupado/libre).
 */
class MesaViewModel(
    private val repository: MesaRepository = MesaRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "MesaViewModel"
    }

    /**
     * Recupera el listado completo de mesas registradas en el sistema.
     * @return [LiveData] que emite la lista de [Mesa] o estados de carga/error.
     */
    fun cargarMesas() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando mesas")
            emit("cargando")

            val resultado = repository.obtenerMesas()

            resultado.onSuccess { mesas ->
                Log.d(TAG, "Se cargaron ${mesas.size} mesas")
                emit(mesas)
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
     * Obtiene únicamente las mesas que se encuentran en estado 'disponible'.
     * @return [LiveData] con la lista de mesas libres.
     */
    fun cargarMesasDisponibles() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando mesas disponibles")
            emit("cargando")

            val resultado = repository.obtenerMesasDisponibles()

            resultado.onSuccess { mesas ->
                Log.d(TAG, "Mesas disponibles: ${mesas.size}")
                emit(mesas)
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
     * Obtiene las mesas que están actualmente en uso por clientes o huéspedes.
     * @return [LiveData] con la lista de mesas ocupadas.
     */
    fun cargarMesasOcupadas() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando mesas ocupadas")
            emit("cargando")

            val resultado = repository.obtenerMesasOcupadas()

            resultado.onSuccess { mesas ->
                Log.d(TAG, "Mesas ocupadas: ${mesas.size}")
                emit(mesas)
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
     * Registra una nueva mesa física en el sistema de gestión.
     * @param numero Número identificador de la mesa.
     * @param capacidad Cantidad máxima de personas.
     * @param ubicacion Descripción del área (ej. "Terraza", "Salón Principal").
     * @return [LiveData] con el resultado de la creación.
     */
    fun crearMesa(numero: Int, capacidad: Int, ubicacion: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando mesa: $numero")
            emit("cargando")

            val resultado = repository.crearMesa(numero, capacidad, ubicacion)

            resultado.onSuccess { id ->
                Log.d(TAG, "Mesa creada: $id")
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
     * Elimina permanentemente una mesa del catálogo.
     * @param mesaId ID único de la mesa a borrar.
     * @return [LiveData] con el estado de la operación.
     */
    fun eliminarMesa(mesaId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Eliminando mesa: $mesaId")
            emit("cargando")

            val resultado = repository.eliminarMesa(mesaId)

            resultado.onSuccess {
                Log.d(TAG, "Mesa eliminada")
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
     * Cambia el estado de una mesa a 'ocupada' y vincula un huésped o pedido.
     * @param mesaId ID de la mesa.
     * @param huespedId ID del huésped (si aplica).
     * @param pedidoId ID del pedido activo en la mesa.
     * @return [LiveData] con la notificación del cambio.
     */
    fun ocuparMesa(mesaId: String, huespedId: String, pedidoId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Ocupando mesa: $mesaId")
            emit("cargando")

            val resultado = repository.ocuparMesa(mesaId, huespedId, pedidoId)

            resultado.onSuccess {
                Log.d(TAG, "Mesa ocupada")
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
     * Restablece el estado de una mesa a 'disponible' tras finalizar el servicio.
     * @param mesaId ID de la mesa a liberar.
     * @return [LiveData] con el resultado del proceso.
     */
    fun liberarMesa(mesaId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Liberando mesa: $mesaId")
            emit("cargando")

            val resultado = repository.liberarMesa(mesaId)

            resultado.onSuccess {
                Log.d(TAG, "Mesa liberada")
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