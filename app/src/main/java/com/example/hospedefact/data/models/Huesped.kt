package com.example.hospedefact.data.models

/**
 * Modelo de Huésped
 * Representa a un cliente que se hospeda en el hotel
 */
data class Huesped(
    val id: String = "",
    val nombre: String = "",
    val habitacion: String = "",
    val habitacionId: String = "",
    val estado: String = "activo",
    val fechaEntrada: Long = System.currentTimeMillis(),
    val fechaSalida: Long? = null,
    val precioNocheHabitacion: Double = 0.0,
    val activo: Boolean = true,
    val email: String = "",
    val telefono: String = ""
)