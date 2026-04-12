package com.example.hospedefact.ui.almacen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.OrdenCompra
import com.example.hospedefact.data.models.ItemOrdenCompra
import com.example.hospedefact.data.repository.OrdenCompraRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel para gestión de Órdenes de Compra
 */
class OrdenCompraViewModel(
    private val repository: OrdenCompraRepository = OrdenCompraRepository()
) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "OrdenCompraViewModel"
    }

    /**
     * Crea nueva orden de compra
     */
    fun crearOrdenCompra(
        proveedorId: String,
        proveedorNombre: String,
        items: List<ItemOrdenCompra>,
        subtotal: Double,
        impuestos: Double,
        total: Double,
        fechaEntregaEsperada: Long,
        notas: String
    ) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando orden de compra a: $proveedorNombre")
            emit("cargando")

            if (items.isEmpty()) {
                emit("error: Agrega al menos un producto")
                return@liveData
            }

            val usuarioId = firebaseAuth.currentUser?.uid ?: ""

            val orden = OrdenCompra(
                proveedorId = proveedorId,
                proveedorNombre = proveedorNombre,
                items = items,
                subtotal = subtotal,
                impuestos = impuestos,
                total = total,
                fechaEntregaEsperada = fechaEntregaEsperada,
                notas = notas,
                creadoPor = usuarioId
            )

            val resultado = repository.crearOrdenCompra(orden)

            resultado.onSuccess { id ->
                Log.d(TAG, "Orden creada: $id")
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
     * Obtiene órdenes pendientes
     */
    fun obtenerOrdenesPendientes() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando órdenes pendientes")
            emit("cargando")

            val resultado = repository.obtenerOrdenesPendientes()

            resultado.onSuccess { ordenes ->
                Log.d(TAG, "Se cargaron ${ordenes.size} órdenes")
                emit(ordenes)
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
     * Obtiene todas las órdenes
     */
    fun obtenerTodasOrdenes() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando todas las órdenes")
            emit("cargando")

            val resultado = repository.obtenerTodasOrdenes()

            resultado.onSuccess { ordenes ->
                Log.d(TAG, "Total de órdenes: ${ordenes.size}")
                emit(ordenes)
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
     * ⭐ CORE: Recibe mercancía (marca orden como entregada)
     * Actualiza automáticamente el stock de productos
     */
    fun recibirMercancia(ordenId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Recibiendo mercancía de orden: $ordenId")
            emit("cargando")

            val resultado = repository.recibirMercancia(ordenId)

            resultado.onSuccess {
                Log.d(TAG, "Mercancía recibida")
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
     * Cambia estado de orden (de pendiente a confirmada)
     */
    fun cambiarEstadoOrden(ordenId: String, nuevoEstado: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cambiando estado de orden: $ordenId -> $nuevoEstado")
            emit("cargando")

            // Obtener la orden
            val ordenResult = repository.obtenerOrdenPorId(ordenId)

            if (ordenResult.isSuccess) {
                val orden = ordenResult.getOrNull()
                if (orden != null) {
                    // Actualizar estado
                    val ordenActualizada = orden.copy(estado = nuevoEstado)
                    val resultado = repository.actualizarEstadoOrden(ordenId, nuevoEstado)

                    resultado.onSuccess {
                        Log.d(TAG, "Estado actualizado")
                        emit("exito")
                    }

                    resultado.onFailure { exception ->
                        Log.e(TAG, "Error", exception)
                        emit("error: ${exception.message}")
                    }
                } else {
                    emit("error: Orden no encontrada")
                }
            } else {
                emit("error: ${ordenResult.exceptionOrNull()?.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }
}