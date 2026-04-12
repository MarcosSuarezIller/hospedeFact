package com.example.hospedefact.data.models

/**
 * Modelo MovimientoStock
 * Registra cada movimiento de stock (entrada/salida)
 * Auditoría completa del almacén
 */
data class MovimientoStock(
    val id: String = "",                         // ID único
    val productoId: String = "",                // ID del producto
    val productoNombre: String = "",            // Nombre del producto
    val tipo: String = "entrada",               // "entrada", "salida", "ajuste", "perdida"
    val cantidad: Int = 0,                      // Cantidad movida
    val stockAnterior: Int = 0,                 // Stock antes del movimiento
    val stockNuevo: Int = 0,                    // Stock después del movimiento
    val fecha: Long = System.currentTimeMillis(), // Cuándo ocurrió
    val referencia: String = "",                // "Orden compra #123", "Pedido #456", "Ajuste manual"
    val razon: String = "",                     // Por qué ocurrió
    val usuarioId: String = "",                 // Quién lo registró
    val notas: String = ""                      // Notas adicionales
)

/**
 * Tipos de movimiento disponibles
 */
enum class TipoMovimiento {
    ENTRADA,      // Entrada de stock (compra, recepción)
    SALIDA,       // Salida de stock (venta, pedido)
    AJUSTE,       // Ajuste de inventario
    PERDIDA,      // Pérdida, daño, robo
    TRANSFERENCIA // Transferencia entre almacenes
}