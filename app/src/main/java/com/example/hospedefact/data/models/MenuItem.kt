package com.example.hospedefact.data.models

/**
 * Modelo de MenuItem
 * Representa un item del menú del restaurante
 */
data class MenuItem(
    val id: String = "",                 // ID único
    val nombre: String = "",             // Ej: "Hamburguesa"
    val descripcion: String = "",        // Ej: "Hamburguesa completa con queso"
    val precio: Double = 0.0,            // Precio en euros
    val categoria: String = "comida",    // "comida", "bebida", "postre", etc
    val activo: Boolean = true           // Si está disponible en el menú
)
