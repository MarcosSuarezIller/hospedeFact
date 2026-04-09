package com.example.hospedefact.data.models

/**
 * Modelo de Factura
 * Representa la factura final consolidada de un huésped
 * Incluye: alojamiento, pedidos de restaurante, extras
 */
data class Factura(
    val id: String = "",                            // ID único
    val huespedId: String = "",                     // A qué huésped pertenece
    val fechaEmision: Long = System.currentTimeMillis(),  // Cuándo se emitió
    val items: List<LineaFactura> = emptyList(),   // Detalles de la factura
    val subtotal: Double = 0.0,                     // Total sin IVA
    val iva: Double = 0.0,                          // IVA (21%)
    val total: Double = 0.0,                        // Total con IVA
    val generadaPor: String = "",                   // UID del usuario que la generó
    val estado: String = "emitida"                  // "emitida", "pagada"
)

/**
 * Modelo de LineaFactura
 * Representa una línea de la factura
 * Ejemplo: Cerveza, 2 unidades, €4.50 cada una = €9.00
 */
data class LineaFactura(
    val descripcion: String = "",          // Descripción del item
    val cantidad: Int = 1,                 // Cantidad
    val precioUnitario: Double = 0.0,     // Precio unitario
    val subtotal: Double = 0.0             // cantidad x precioUnitario
)