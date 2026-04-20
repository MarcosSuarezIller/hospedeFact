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
     * Registra un nuevo producto en el catálogo del almacén.
     * 
     * @param producto Objeto [ProductoAlmacen] con la información del artículo y límites de stock.
     * @return [Result] con el ID del documento creado en Firestore.
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
     * Recupera todos los productos del almacén que se encuentran en estado activo.
     * 
     * @return [Result] con la lista completa de productos disponibles.
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
     * Busca un producto específico en el almacén mediante su identificador único.
     * 
     * @param productoId ID único del producto.
     * @return [Result] con el objeto [ProductoAlmacen] si existe, o null.
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
     * Actualiza la información técnica o de inventario de un producto existente.
     * 
     * @param producto Objeto [ProductoAlmacen] con los datos actualizados.
     * @return [Result] indicando el éxito o fallo de la actualización.
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
     * Disminuye la cantidad de stock disponible de un producto.
     * Verifica que exista stock suficiente antes de proceder y registra el movimiento 
     * en el historial para auditoría.
     * 
     * @param productoId ID del producto a descontar.
     * @param cantidad Unidades a retirar del almacén.
     * @param referencia Código o descripción de referencia (ej. ID del pedido).
     * @return [Result] indicando el éxito de la operación o error por stock insuficiente.
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
     * Incrementa el stock de un producto, usualmente tras la recepción de una compra a proveedor.
     * Registra el movimiento en el historial y lanza una advertencia si se supera el stock máximo definido.
     * 
     * @param productoId ID del producto a incrementar.
     * @param cantidad Unidades a añadir al almacén.
     * @param referencia Código de referencia (ej. ID de la orden de compra).
     * @return [Result] indicando el éxito de la operación.
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
     * Registra de forma persistente cualquier cambio en el inventario en la colección de movimientos.
     * Permite mantener una trazabilidad completa de entradas y salidas.
     * 
     * @param productoId ID del producto.
     * @param productoNombre Nombre del producto en el momento del movimiento.
     * @param tipo Tipo de movimiento ("entrada" o "salida").
     * @param cantidad Cantidad involucrada.
     * @param stockAnterior Cantidad de stock antes de la operación.
     * @param stockNuevo Cantidad de stock resultante.
     * @param referencia Documento o ID de referencia.
     * @param razon Motivo del movimiento (Venta, Compra, Ajuste, etc.).
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
     * Filtra y devuelve la lista de productos cuyo stock actual es inferior al stock mínimo de seguridad.
     * 
     * @return [Result] con la lista de productos que requieren reposición.
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
     * Recupera el historial cronológico de movimientos (entradas/salidas) de un producto específico.
     * 
     * @param productoId ID único del producto.
     * @return [Result] con la lista de movimientos ordenada por fecha (más reciente primero).
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
     * Calcula el valor monetario total de todas las existencias actuales en el almacén 
     * basándose en el precio de compra.
     * 
     * @return [Result] con el valor total acumulado del inventario.
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
