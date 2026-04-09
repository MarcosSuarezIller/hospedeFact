package com.example.hospedefact.data.models

/**
 * Modelo de Pedido
 * Representa un pedido del restaurante hecho por un huésped
 */
data class Pedido(
    val id: String = "",                           // ID único
    val huespedId: String = "",                    // A qué huésped pertenece
    val items: List<ItemPedido> = emptyList(),   // Lista de items del pedido
    val fecha: Long = System.currentTimeMillis(), // Cuándo se hizo el pedido
    val total: Double = 0.0,                       // Total del pedido
    val estado: String = "pendiente"               // "pendiente" o "facturado"
)

/**
 * Modelo de ItemPedido
 * Representa un item dentro de un pedido
 * Ejemplo: 2x Cerveza a €4.50 = €9.00
 */
data class ItemPedido(
    val itemId: String = "",                // ID del MenuItem
    val nombre: String = "",                // Nombre del item (para referencia)
    val cantidad: Int = 1,                  // Cuántos se pidieron
    val precioUnitario: Double = 0.0        // Precio de uno
)
