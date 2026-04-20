package com.example.hospedefact.data.repository

import android.util.Log
import com.example.hospedefact.data.models.OrdenCompra
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para gestión de Órdenes de Compra
 * Control de compras a proveedores
 */
class OrdenCompraRepository(
    private val productoRepository: ProductoAlmacenRepository = ProductoAlmacenRepository()
) {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("ordenes_compra")

    companion object {
        private const val TAG = "OrdenCompraRepository"
    }

    /**
     * Registra una nueva orden de compra en la base de datos.
     * 
     * @param orden Objeto [OrdenCompra] con la información del proveedor e ítems a comprar.
     * @return [Result] con el ID del documento generado en Firestore.
     */
    suspend fun crearOrdenCompra(orden: OrdenCompra): Result<String> = try {
        Log.d(TAG, "Creando orden de compra para: ${orden.proveedorNombre}")

        val doc = coleccion.document()
        val ordenConId = orden.copy(id = doc.id)
        doc.set(ordenConId).await()

        Log.d(TAG, "Orden de compra creada: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear orden de compra", e)
        Result.failure(e)
    }

    /**
     * Recupera todas las órdenes de compra que se encuentran en estado "pendiente".
     * Las órdenes se devuelven ordenadas por fecha de forma descendente.
     * 
     * @return [Result] con la lista de órdenes de compra pendientes.
     */
    suspend fun obtenerOrdenesPendientes(): Result<List<OrdenCompra>> = try {
        Log.d(TAG, "Obteniendo órdenes pendientes")

        val snapshot = coleccion
            .whereEqualTo("estado", "pendiente")
            //.orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val ordenes = snapshot.toObjects(OrdenCompra::class.java)
            .sortedByDescending { it.fecha }

        Log.d(TAG, "Se obtuvieron ${ordenes.size} órdenes pendientes")
        Result.success(ordenes)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener órdenes pendientes", e)
        Result.failure(e)
    }

    /**
     * Obtiene el listado completo de órdenes de compra registradas en el sistema.
     * Ordenadas cronológicamente desde la más reciente.
     * 
     * @return [Result] con la lista histórica de órdenes de compra.
     */
    suspend fun obtenerTodasOrdenes(): Result<List<OrdenCompra>> = try {
        Log.d(TAG, "Obteniendo todas las órdenes de compra")

        val snapshot = coleccion
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val ordenes = snapshot.toObjects(OrdenCompra::class.java)
        Log.d(TAG, "Total de órdenes: ${ordenes.size}")
        Result.success(ordenes)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener todas las órdenes", e)
        Result.failure(e)
    }

    /**
     * Busca una orden de compra específica mediante su identificador único.
     * 
     * @param ordenId ID único de la orden de compra.
     * @return [Result] con el objeto [OrdenCompra] si se encuentra, o null.
     */
    suspend fun obtenerOrdenPorId(ordenId: String): Result<OrdenCompra?> = try {
        Log.d(TAG, "Obteniendo orden: $ordenId")

        val doc = coleccion.document(ordenId).get().await()
        val orden = doc.toObject(OrdenCompra::class.java)

        Result.success(orden)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener orden", e)
        Result.failure(e)
    }

    /**
     * Actualiza el estado de una orden de compra (ej. "pendiente", "confirmada", "entregada").
     * 
     * @param ordenId ID de la orden a actualizar.
     * @param nuevoEstado El nuevo estado que se asignará.
     * @return [Result] que indica el éxito o fallo de la actualización.
     */
    suspend fun actualizarEstadoOrden(ordenId: String, nuevoEstado: String): Result<Unit> = try {
        Log.d(TAG, "Actualizando estado de orden: $ordenId -> $nuevoEstado")

        coleccion.document(ordenId).update("estado", nuevoEstado).await()

        Log.d(TAG, "Estado actualizado")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar estado", e)
        Result.failure(e)
    }

    /**
     * Cambia el flujo de estado de una orden de compra.
     * Típicamente sigue el flujo: pendiente -> confirmada -> entregada.
     * 
     * @param ordenId ID de la orden.
     * @param nuevoEstado Estado al que se desea transicionar.
     * @return [Result] que indica el resultado de la transición.
     */
    suspend fun cambiarEstadoOrden(ordenId: String, nuevoEstado: String): Result<Unit> = try {
        Log.d(TAG, "Cambiando estado de orden: $ordenId -> $nuevoEstado")

        val resultado = actualizarEstadoOrden(ordenId, nuevoEstado)

        if (resultado.isSuccess) {
            Log.d(TAG, "Estado actualizado exitosamente")
            Result.success(Unit)
        } else {
            Result.failure(resultado.exceptionOrNull() ?: Exception("Error desconocido"))
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error al cambiar estado", e)
        Result.failure(e)
    }

    /**
     * Procesa la recepción física de los productos de una orden de compra.
     * Cambia el estado de la orden a "entregada", registra la fecha de recepción
     * y actualiza automáticamente el stock de cada producto en el almacén.
     * 
     * @param ordenId ID de la orden cuyos productos se están recibiendo.
     * @return [Result] indicando el éxito del proceso de recepción y actualización de stock.
     */
    suspend fun recibirMercancia(ordenId: String): Result<Unit> = try {
        Log.d(TAG, "Recibiendo mercancía de orden: $ordenId")

        val ordenResult = obtenerOrdenPorId(ordenId)

        if (ordenResult.isFailure) {
            throw Exception("Orden no encontrada")
        }

        val orden = ordenResult.getOrNull()
            ?: throw Exception("Orden no encontrada")

        // Actualizar stock de cada producto
        for (item in orden.items) {
            Log.d(TAG, "Agregando ${item.cantidad} unidades de ${item.productoNombre}")

            val resultado = productoRepository.agregarStock(
                productoId = item.productoId,
                cantidad = item.cantidadRecibida,
                referencia = "Orden de compra #${orden.id.take(8)}"
            )

            if (resultado.isFailure) {
                Log.e(TAG, "Error al agregar stock del producto ${item.productoId}")
                throw Exception("Error al agregar stock: ${resultado.exceptionOrNull()?.message}")
            }
        }

        // Marcar orden como entregada
        val ordenActualizada = orden.copy(
            estado = "entregada",
            fechaEntregaReal = System.currentTimeMillis()
        )

        coleccion.document(ordenId).set(ordenActualizada).await()

        Log.d(TAG, "Mercancía recibida y stock actualizado")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al recibir mercancía", e)
        Result.failure(e)
    }

    /**
     * Recupera todas las órdenes de compra asociadas a un proveedor en particular.
     * 
     * @param proveedorId ID único del proveedor.
     * @return [Result] con la lista de órdenes de compra vinculadas a dicho proveedor.
     */
    suspend fun obtenerOrdenesPorProveedor(proveedorId: String): Result<List<OrdenCompra>> = try {
        Log.d(TAG, "Obteniendo órdenes del proveedor: $proveedorId")

        val snapshot = coleccion
            .whereEqualTo("proveedorId", proveedorId)
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val ordenes = snapshot.toObjects(OrdenCompra::class.java)
        Log.d(TAG, "Se obtuvieron ${ordenes.size} órdenes")
        Result.success(ordenes)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener órdenes del proveedor", e)
        Result.failure(e)
    }
}