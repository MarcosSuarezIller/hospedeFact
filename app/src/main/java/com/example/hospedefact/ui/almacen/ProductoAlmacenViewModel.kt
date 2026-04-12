package com.example.hospedefact.ui.almacen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.ProductoAlmacen
import com.example.hospedefact.data.models.MovimientoStock
import com.example.hospedefact.data.repository.ProductoAlmacenRepository
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel para gestión de Productos en Almacén
 */
class ProductoAlmacenViewModel(
    private val repository: ProductoAlmacenRepository = ProductoAlmacenRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "ProductoAlmacenViewModel"
    }

    /**
     * Crea nuevo producto en almacén
     */
    fun crearProductoAlmacen(
        nombre: String,
        descripcion: String,
        proveedor: String,
        stockMinimo: Int,
        stockMaximo: Int,
        unidad: String,
        precioCompra: Double,
        precioVenta: Double,
        ubicacion: String
    ) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando producto: $nombre")
            emit("cargando")

            if (nombre.isEmpty() || precioCompra <= 0.0 || precioVenta <= 0.0) {
                emit("error: Completa todos los campos correctamente")
                return@liveData
            }

            val producto = ProductoAlmacen(
                nombre = nombre,
                descripcion = descripcion,
                proveedor = proveedor,
                stockMinimo = stockMinimo,
                stockMaximo = stockMaximo,
                unidad = unidad,
                precioCompra = precioCompra,
                precioVenta = precioVenta,
                ubicacion = ubicacion,
                stockActual = 0
            )

            val resultado = repository.crearProductoAlmacen(producto)

            resultado.onSuccess { id ->
                Log.d(TAG, "Producto creado: $id")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al crear producto", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Carga todos los productos del almacén
     */
    fun cargarProductos() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando productos")
            emit("cargando")

            val resultado = repository.obtenerProductos()

            resultado.onSuccess { productos ->
                Log.d(TAG, "Se cargaron ${productos.size} productos")
                emit(productos)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al cargar", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Obtiene productos con stock bajo
     */
    fun obtenerProductosStockBajo() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo productos con stock bajo")
            emit("cargando")

            val resultado = repository.obtenerProductosConStockBajo()

            resultado.onSuccess { productos ->
                Log.d(TAG, "Se encontraron ${productos.size} productos con stock bajo")
                emit(productos)
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
     * Obtiene historial de movimientos de un producto
     */
    fun obtenerMovimientosProducto(productoId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo movimientos del producto: $productoId")
            emit("cargando")

            val resultado = repository.obtenerMovimientosProducto(productoId)

            resultado.onSuccess { movimientos ->
                Log.d(TAG, "Se obtuvieron ${movimientos.size} movimientos")
                emit(movimientos)
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
     * Obtiene valor total del inventario
     */
    fun obtenerValorTotalInventario() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Calculando valor total")
            emit("cargando")

            val resultado = repository.obtenerValorTotalInventario()

            resultado.onSuccess { valor ->
                Log.d(TAG, "Valor total: €$valor")
                emit(valor)
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