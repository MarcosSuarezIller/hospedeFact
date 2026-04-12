package com.example.hospedefact.data.models

/**
 * Modelo Proveedor
 * Representa un proveedor de productos
 */
data class Proveedor(
    val id: String = "",                         // ID único
    val nombre: String = "",                     // Nombre de la empresa
    val contacto: String = "",                   // Nombre del contacto
    val email: String = "",                      // Email
    val telefono: String = "",                   // Teléfono
    val direccion: String = "",                  // Dirección
    val ciudad: String = "",                     // Ciudad
    val pais: String = "",                       // País
    val codigoPostal: String = "",              // Código postal
    val condicionesPago: String = "",            // Ej: "30 días neto"
    val tiempoEntrega: Int = 3,                 // Días de entrega promedio
    val minimo: Double = 0.0,                   // Pedido mínimo
    val descuento: Double = 0.0,                // Descuento estándar (%)
    val estado: String = "activo",              // "activo" o "inactivo"
    val fechaRegistro: Long = System.currentTimeMillis(),
    val activo: Boolean = true,
    val calificacion: Double = 5.0              // Calificación 1-5
)

/**
 * Información de contacto del proveedor
 */
data class ContactoProveedor(
    val proveedorId: String = "",
    val nombre: String = "",
    val puesto: String = "",
    val email: String = "",
    val telefono: String = "",
    val whatsapp: String = ""
)