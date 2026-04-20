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
     * Registra una nueva orden de compra en el sistema.
     * Valida que la orden contenga al menos un producto antes de enviarla al repositorio.
     * 
     * @param proveedorId ID único del proveedor seleccionado.
     * @param proveedorNombre Nombre del proveedor para visualización rápida.
     * @param items Lista de productos y cantidades solicitadas.
     * @param subtotal Suma base de los precios de los productos.
     * @param impuestos Importe correspondiente a los impuestos aplicables.
     * @param total Importe total final de la orden.
     * @param fechaEntregaEsperada Timestamp de la fecha prevista para recibir el pedido.
     * @param notas Comentarios adicionales para el proveedor o almacén.
     * @return [LiveData] con el estado de la transacción ("exito", "cargando", "error:").
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
     * Recupera el listado de órdenes de compra que aún no han sido entregadas.
     * 
     * @return [LiveData] que emite la lista de órdenes pendientes o mensajes de estado.
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
     * Obtiene el historial completo de todas las órdenes de compra emitidas.
     * 
     * @return [LiveData] con la colección de todas las órdenes.
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
     * Procesa la recepción física de los productos incluidos en una orden.
     * Esta acción desencadena la actualización automática del stock de inventario.
     * 
     * @param ordenId ID de la orden que se está recibiendo.
     * @return [LiveData] indicando el éxito del proceso y la actualización de stock.
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
     * Actualiza el estado administrativo de una orden de compra (ej. de "pendiente" a "confirmada").
     * 
     * @param ordenId ID único de la orden.
     * @param nuevoEstado Nombre del nuevo estado a aplicar.
     * @return [LiveData] con el resultado de la operación.
     */
    fun cambiarEstadoOrden(ordenId: String, nuevoEstado: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cambiando estado: $ordenId -> $nuevoEstado")
            emit("cargando")

            val resultado = repository.cambiarEstadoOrden(ordenId, nuevoEstado)

            resultado.onSuccess {
                Log.d(TAG, "Estado cambiado exitosamente")
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