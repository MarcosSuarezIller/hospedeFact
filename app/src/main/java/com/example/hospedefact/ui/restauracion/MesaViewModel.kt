package com.example.hospedefact.ui.restauracion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.Mesa
import com.example.hospedefact.data.repository.MesaRepository
import kotlinx.coroutines.Dispatchers

class MesaViewModel(
    private val repository: MesaRepository = MesaRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "MesaViewModel"
    }

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