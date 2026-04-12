package com.example.hospedefact.data.models

/**
 * Modelo ProductoAlmacen
 * Representa un producto en el almacén con información de stock
 * Vinculado al MenuItem pero con control de inventario
 */
data class ProductoAlmacen(
    val id: String = "",                          // ID único del producto
    val itemMenuId: String = "",                  // Referencia al MenuItem
    val nombre: String = "",                      // Nombre del producto
    val descripcion: String = "",                 // Descripción
    val proveedor: String = "",                   // ID del proveedor principal
    val stockActual: Int = 0,                     // Cantidad en stock
    val stockMinimo: Int = 5,                     // Stock mínimo para alerta
    val stockMaximo: Int = 100,                   // Stock máximo permitido
    val unidad: String = "unidades",              // Unidad de medida (kg, litros, etc)
    val precioCompra: Double = 0.0,              // Precio de compra
    val precioVenta: Double = 0.0,               // Precio de venta
    val ubicacion: String = "",                  // Ubicación en almacén (Ej: A-1-3)
    val estado: String = "activo",               // "activo" o "descontinuado"
    val fechaUltimaActualizacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true
)

/**
 * Estado del stock para alertas
 */
enum class EstadoStock {
    NORMAL,      // Stock en rango normal
    BAJO,        // Stock por debajo del mínimo
    CRITICO,     // Stock muy bajo
    AGOTADO,     // Sin stock
    EXCESO       // Stock por encima del máximo
}

/**
 * Calcula el estado del stock
 */
fun ProductoAlmacen.obtenerEstadoStock(): EstadoStock {
    return when {
        stockActual <= 0 -> EstadoStock.AGOTADO
        stockActual < (stockMinimo / 2) -> EstadoStock.CRITICO
        stockActual < stockMinimo -> EstadoStock.BAJO
        stockActual > stockMaximo -> EstadoStock.EXCESO
        else -> EstadoStock.NORMAL
    }
}

/**
 * Calcula la utilidad de un producto
 */
fun ProductoAlmacen.calcularUtilidad(): Double {
    return precioVenta - precioCompra
}

/**
 * Obtiene el valor total del stock
 */
fun ProductoAlmacen.obtenerValorTotalStock(): Double {
    return stockActual * precioCompra
}