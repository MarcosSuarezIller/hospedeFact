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
     * CREAR: Nueva orden de compra
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
     * LEER: Obtener todas las órdenes pendientes
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
     * LEER: Obtener todas las órdenes
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
     * LEER: Obtener orden por ID
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
     * ACTUALIZAR: Estado de orden (confirmada, entregada, etc)
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
     * CORE: Recibir mercancía (marcar orden como entregada)
     * Automáticamente agrega stock a los productos
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
     * OBTENER: Órdenes de un proveedor específico
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