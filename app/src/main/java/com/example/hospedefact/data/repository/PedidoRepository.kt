package com.example.hospedefact.data.repository

import android.util.Log
import com.example.hospedefact.data.models.ItemPedido
import com.example.hospedefact.data.models.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para manejar Pedidos en Firestore
 * CRUD: Crear, Leer, Actualizar, Eliminar pedidos
 */
class PedidoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("pedidos")

    companion object {
        private const val TAG = "PedidoRepository"
    }

    /**
     * Crea un nuevo pedido en Firestore
     */
    suspend fun crearPedido(pedido: Pedido): Result<String> = try {
        Log.d(TAG, "Creando pedido para huésped: ${pedido.huespedId}")

        val doc = coleccion.document()
        val pedidoConId = pedido.copy(id = doc.id)
        doc.set(pedidoConId).await()

        Log.d(TAG, "Pedido creado: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear pedido", e)
        Result.failure(e)
    }

    /**
     * Obtiene todos los pedidos pendientes de un huésped
     */
    suspend fun obtenerPedidosPorHuesped(huespedId: String): Result<List<Pedido>> = try {
        Log.d(TAG, "Obteniendo pedidos pendientes del huésped: $huespedId")

        val snapshot = coleccion
            .whereEqualTo("huespedId", huespedId)
            .whereEqualTo("estado", "pendiente")
            .get()
            .await()

        val pedidos = snapshot.toObjects(Pedido::class.java)
        Log.d(TAG, "Se obtuvieron ${pedidos.size} pedidos")
        Result.success(pedidos)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener pedidos", e)
        Result.failure(e)
    }

    /**
     * Obtiene un pedido específico por ID
     */
    suspend fun obtenerPedidoPorId(pedidoId: String): Result<Pedido?> = try {
        Log.d(TAG, "Obteniendo pedido: $pedidoId")

        val doc = coleccion.document(pedidoId).get().await()
        val pedido = doc.toObject(Pedido::class.java)

        Log.d(TAG, "Pedido encontrado: $pedidoId")
        Result.success(pedido)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener pedido", e)
        Result.failure(e)
    }

    /**
     * Actualiza un pedido (ej: cambiar estado a facturado)
     */
    suspend fun actualizarPedido(pedido: Pedido): Result<Unit> = try {
        Log.d(TAG, "Actualizando pedido: ${pedido.id}")

        coleccion.document(pedido.id).set(pedido).await()

        Log.d(TAG, "Pedido actualizado")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar pedido", e)
        Result.failure(e)
    }

    /**
     * Obtiene todos los pedidos (incluyendo facturados)
     * Útil para reportes
     */
    suspend fun obtenerTodosPedidos(): Result<List<Pedido>> = try {
        Log.d(TAG, "Obteniendo todos los pedidos")

        val snapshot = coleccion.get().await()
        val pedidos = snapshot.toObjects(Pedido::class.java)

        Log.d(TAG, "Total de pedidos: ${pedidos.size}")
        Result.success(pedidos)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener todos los pedidos", e)
        Result.failure(e)
    }

    /**
     * Obtiene pedidos facturados de un huésped
     */
    suspend fun obtenerPedidosFacturados(huespedId: String): Result<List<Pedido>> = try {
        Log.d(TAG, "Obteniendo pedidos facturados del huésped: $huespedId")

        val snapshot = coleccion
            .whereEqualTo("huespedId", huespedId)
            .whereEqualTo("estado", "facturado")
            .get()
            .await()

        val pedidos = snapshot.toObjects(Pedido::class.java)
        Log.d(TAG, "Se obtuvieron ${pedidos.size} pedidos facturados")
        Result.success(pedidos)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener pedidos facturados", e)
        Result.failure(e)
    }
}
