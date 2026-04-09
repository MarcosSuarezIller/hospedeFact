package com.example.hospedefact.data.models

/**
 * Modelo de Huésped
 * Representa a un cliente que se hospeda en el hotel
 */
data class Huesped(
    val id: String = "",                              // ID único de Firestore
    val nombre: String = "",                          // Nombre del huésped
    val email: String = "",                           // Email
    val telefono: String = "",                        // Teléfono de contacto
    val habitacion: String = "",                      // Número de habitación
    val fechaEntrada: Long = 0,                       // Cuándo llega (timestamp)
    val fechaSalida: Long = 0,                        // Cuándo se va (timestamp)
    val estado: String = "activo"                     // "activo" o "checkout"
)