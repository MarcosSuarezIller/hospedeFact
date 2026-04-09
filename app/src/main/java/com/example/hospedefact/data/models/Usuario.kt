package com.example.hospedefact.data.models

/**
 * Modelo de Usuario para autenticación
 * Representa a un empleado del negocio (Gerente, Camarero)
 */

data class Usuario(
    val uid: String = "",           // ID único de Firebase Auth
    val nombre: String = "",         // Nombre del empleado
    val email: String = "",          // Email
    val rol: String = "camarero",   // "gerente" o "camarero"
    val activo: Boolean = true,      // Si la cuenta está activa
    val fechaAlta: Long = System.currentTimeMillis()  // Cuándo se creó
)
