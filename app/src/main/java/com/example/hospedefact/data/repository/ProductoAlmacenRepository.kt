package com.example.hospedefact.data.repository


import android.util.Log
import com.example.hospedefact.data.models.EstadoStock
import com.example.hospedefact.data.models.ProductoAlmacen
import com.example.hospedefact.data.models.MovimientoStock
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para gestión de Productos en el Almacén
 * Controla stock, movimientos y alertas
 */
class ProductoAlmacenRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccionProductos = db.collection("productos_almacen")
    private val coleccionMovimientos = db.collection("movimientos_stock")

    companion object {
        private const val TAG = "ProductoAlmacenRepository"
    }

    /**
     * CREAR: Nuevo producto en almacén
     */
    suspend fun crearProductoAlmacen(producto: ProductoAlmacen): Result<String> = try {
        Log.d(TAG, "Creando producto: ${producto.nombre}")

        val doc = coleccionProductos.document()
        val productoConId = producto.copy(id = doc.id)
        doc.set(productoConId).await()

        Log.d(TAG, "Producto creado: ${doc.id}")
        Result.success(doc.id)

    } catch (e: Exception) {
        Log.e(TAG, "Error al crear producto", e)
        Result.failure(e)
    }

    /**
     * LEER: Obtener todos los productos
     */
    suspend fun obtenerProductos(): Result<List<ProductoAlmacen>> = try {
        Log.d(TAG, "Obteniendo productos del almacén")

        val snapshot = coleccionProductos
            .whereEqualTo("activo", true)
            .get()
            .await()

        val productos = snapshot.toObjects(ProductoAlmacen::class.java)
        Log.d(TAG, "Se obtuvieron ${productos.size} productos")
        Result.success(productos)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener productos", e)
        Result.failure(e)
    }

    /**
     * LEER: Obtener producto por ID
     */
    suspend fun obtenerProductoPorId(productoId: String): Result<ProductoAlmacen?> = try {
        Log.d(TAG, "Obteniendo producto: $productoId")

        val doc = coleccionProductos.document(productoId).get().await()
        val producto = doc.toObject(ProductoAlmacen::class.java)

        Result.success(producto)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener producto", e)
        Result.failure(e)
    }

    /**
     * ACTUALIZAR: Actualizar datos de producto
     */
    suspend fun actualizarProducto(producto: ProductoAlmacen): Result<Unit> = try {
        Log.d(TAG, "Actualizando producto: ${producto.id}")

        coleccionProductos.document(producto.id).set(producto).await()

        Log.d(TAG, "Producto actualizado")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al actualizar producto", e)
        Result.failure(e)
    }

    /**
     * ⭐ CORE: Descontar stock cuando se crea un pedido
     * Valida que hay suficiente stock antes de descontar
     */
    suspend fun descontarStock(
        productoId: String,
        cantidad: Int,
        referencia: String
    ): Result<Unit> = try {
        Log.d(TAG, "Descontando stock: producto=$productoId, cantidad=$cantidad")

        // Obtener producto
        val productoResult = obtenerProductoPorId(productoId)

        if (productoResult.isFailure) {
            throw Exception("Producto no encontrado")
        }

        val producto = productoResult.getOrNull()
            ?: throw Exception("Producto no encontrado")

        // Validar stock suficiente
        if (producto.stockActual < cantidad) {
            throw Exception("Stock insuficiente. Disponible: ${producto.stockActual}, Solicitado: $cantidad")
        }

        Log.d(TAG, "Stock disponible, procediendo a descontar")

        // Actualizar stock
        val stockNuevo = producto.stockActual - cantidad
        val productoActualizado = producto.copy(
            stockActual = stockNuevo,
            fechaUltimaActualizacion = System.currentTimeMillis()
        )

        // Guardar cambio
        coleccionProductos.document(productoId).set(productoActualizado).await()

        // Registrar movimiento
        registrarMovimiento(
            productoId = productoId,
            productoNombre = producto.nombre,
            tipo = "salida",
            cantidad = cantidad,
            stockAnterior = producto.stockActual,
            stockNuevo = stockNuevo,
            referencia = referencia,
            razon = "Venta (pedido)"
        )

        Log.d(TAG, "Stock descontado exitosamente. Nuevo stock: $stockNuevo")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al descontar stock", e)
        Result.failure(e)
    }

    /**
     * ⭐ CORE: Agregar stock (recepción de compra)
     * Cuando llega mercancía del proveedor
     */
    suspend fun agregarStock(
        productoId: String,
        cantidad: Int,
        referencia: String
    ): Result<Unit> = try {
        Log.d(TAG, "Agregando stock: producto=$productoId, cantidad=$cantidad")

        // Obtener producto
        val productoResult = obtenerProductoPorId(productoId)

        if (productoResult.isFailure) {
            throw Exception("Producto no encontrado")
        }

        val producto = productoResult.getOrNull()
            ?: throw Exception("Producto no encontrado")

        // Validar que no exceda máximo
        val stockNuevo = producto.stockActual + cantidad

        if (stockNuevo > producto.stockMaximo) {
            Log.w(TAG, "⚠️ Atención: Stock superará el máximo permitido ($stockNuevo > ${producto.stockMaximo})")
        }

        // Actualizar stock
        val productoActualizado = producto.copy(
            stockActual = stockNuevo,
            fechaUltimaActualizacion = System.currentTimeMillis()
        )

        coleccionProductos.document(productoId).set(productoActualizado).await()

        // Registrar movimiento
        registrarMovimiento(
            productoId = productoId,
            productoNombre = producto.nombre,
            tipo = "entrada",
            cantidad = cantidad,
            stockAnterior = producto.stockActual,
            stockNuevo = stockNuevo,
            referencia = referencia,
            razon = "Compra a proveedor"
        )

        Log.d(TAG, "Stock agregado exitosamente. Nuevo stock: $stockNuevo")
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e(TAG, "Error al agregar stock", e)
        Result.failure(e)
    }

    /**
     * REGISTRAR MOVIMIENTO: Crea registro de cada cambio de stock
     * Para auditoría completa
     */
    private suspend fun registrarMovimiento(
        productoId: String,
        productoNombre: String,
        tipo: String,
        cantidad: Int,
        stockAnterior: Int,
        stockNuevo: Int,
        referencia: String,
        razon: String
    ) {
        try {
            Log.d(TAG, "Registrando movimiento: $tipo de $cantidad unidades")

            val movimiento = MovimientoStock(
                productoId = productoId,
                productoNombre = productoNombre,
                tipo = tipo,
                cantidad = cantidad,
                stockAnterior = stockAnterior,
                stockNuevo = stockNuevo,
                referencia = referencia,
                razon = razon
            )

            val doc = coleccionMovimientos.document()
            coleccionMovimientos.document(doc.id).set(movimiento.copy(id = doc.id)).await()

            Log.d(TAG, "Movimiento registrado: ${doc.id}")

        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar movimiento", e)
        }
    }

    /**
     * OBTENER ALERTAS: Productos con stock bajo
     */
    suspend fun obtenerProductosConStockBajo(): Result<List<ProductoAlmacen>> = try {
        Log.d(TAG, "Obteniendo productos con stock bajo")

        val snapshot = coleccionProductos
            .whereEqualTo("activo", true)
            .get()
            .await()

        val productos = snapshot.toObjects(ProductoAlmacen::class.java)
        val productosBajos = productos.filter { it.stockActual < it.stockMinimo }

        Log.d(TAG, "Encontrados ${productosBajos.size} productos con stock bajo")
        Result.success(productosBajos)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener productos con stock bajo", e)
        Result.failure(e)
    }

    /**
     * OBTENER HISTORIAL: Movimientos de un producto
     */
    suspend fun obtenerMovimientosProducto(productoId: String): Result<List<MovimientoStock>> = try {
        Log.d(TAG, "Obteniendo movimientos del producto: $productoId")

        val snapshot = coleccionMovimientos
            .whereEqualTo("productoId", productoId)
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        val movimientos = snapshot.toObjects(MovimientoStock::class.java)
        Log.d(TAG, "Se obtuvieron ${movimientos.size} movimientos")
        Result.success(movimientos)

    } catch (e: Exception) {
        Log.e(TAG, "Error al obtener movimientos", e)
        Result.failure(e)
    }

    /**
     * OBTENER VALOR TOTAL DEL INVENTARIO
     */
    suspend fun obtenerValorTotalInventario(): Result<Double> = try {
        Log.d(TAG, "Calculando valor total del inventario")

        val snapshot = coleccionProductos
            .whereEqualTo("activo", true)
            .get()
            .await()

        val productos = snapshot.toObjects(ProductoAlmacen::class.java)
        val valorTotal = productos.sumOf { it.stockActual * it.precioCompra }

        Log.d(TAG, "Valor total del inventario: €$valorTotal")
        Result.success(valorTotal)

    } catch (e: Exception) {
        Log.e(TAG, "Error al calcular valor total", e)
        Result.failure(e)
    }
}
