package com.example.hospedefact.data.models

/**
 * Modelo OrdenCompra
 * Representa una orden de compra a un proveedor
 */
data class OrdenCompra(
    val id: String = "",                         // ID único de la orden
    val proveedorId: String = "",               // ID del proveedor
    val proveedorNombre: String = "",           // Nombre del proveedor
    val fecha: Long = System.currentTimeMillis(), // Fecha de creación
    val items: List<ItemOrdenCompra> = emptyList(), // Items ordenados
    val subtotal: Double = 0.0,                 // Total sin impuestos
    val impuestos: Double = 0.0,                // Impuestos
    val total: Double = 0.0,                    // Total
    val estado: String = "pendiente",           // "pendiente", "confirmada", "entregada", "cancelada"
    val fechaEntregaEsperada: Long = 0,        // Fecha esperada de entrega
    val fechaEntregaReal: Long = 0,            // Fecha real de entrega
    val notas: String = "",                     // Notas de la orden
    val creadoPor: String = ""                  // UID del usuario que creó la orden
)

/**
 * Item dentro de una OrdenCompra
 */
data class ItemOrdenCompra(
    val productoId: String = "",                // ID del producto
    val productoNombre: String = "",            // Nombre del producto
    val cantidad: Int = 0,                      // Cantidad ordenada
    val unidad: String = "unidades",            // Unidad de medida
    val precioUnitario: Double = 0.0,          // Precio unitario
    val subtotal: Double = 0.0,                // cantidad * precioUnitario
    val cantidadRecibida: Int = 0              // Cantidad realmente recibida
)