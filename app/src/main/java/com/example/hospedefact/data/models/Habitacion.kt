package com.example.hospedefact.data.models


data class Habitacion(
    val id: String = "",
    val numero: Int = 0,
    val tipo: String = "",
    val precioNoche: Double = 0.0,
    val capacidad: Int = 2,
    val estado: String = "disponible",
    val activo: Boolean = true
)