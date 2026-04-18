package com.example.hospedefact.data.models

/**
 * Modelo Mesa
 * Representa una mesa del restaurante
 */
data class Mesa(
    val id: String = "",                          // ID único
    val numero: Int = 0,                          // Número de mesa (1, 2, 3...)
    val capacidad: Int = 4,                       // Capacidad de personas
    val estado: String = "disponible",            // disponible, ocupada, reservada, mantenimiento
    val huespedId: String? = null,               // ID del huésped si está ocupada
    val pedidoId: String? = null,                // ID del pedido actual
    val fechaOcupacion: Long = 0,                // Cuándo se ocupó
    val ubicacion: String = "",                   // Descripción de ubicación
    val activo: Boolean = true
)

/**
 * Historial de mesa
 * Para auditoría
 */
data class HistorialMesa(
    val id: String = "",
    val mesaId: String = "",
    val accion: String = "",                      // ocupada, liberada, limpieza, etc
    val huespedId: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val notas: String = ""
)

enum class EstadoMesa {
    DISPONIBLE,
    OCUPADA,
    RESERVADA,
    MANTENIMIENTO,
    LIMPIEZA
}