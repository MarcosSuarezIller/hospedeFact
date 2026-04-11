package com.example.hospedefact.ui.factura

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.Factura
import com.example.hospedefact.data.repository.FacturaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel para gestión de Facturas
 * Maneja generación y visualización de facturas
 */
class FacturaViewModel(
    private val repository: FacturaRepository = FacturaRepository()
) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "FacturaViewModel"
    }

    /**
     * GENERA FACTURA CONSOLIDADA
     *
     * Este es el MÉTODO PRINCIPAL del proyecto
     * Consolida todos los pedidos pendientes en UNA factura
     *
     * @param huespedId ID del huésped
     * @return LiveData que emite la factura generada
     */
    fun generarFactura(huespedId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando generación de factura para huésped: $huespedId")

            // Emite estado de carga
            emit("cargando")

            // Obtiene UID del usuario actual
            val usuarioId = firebaseAuth.currentUser?.uid
                ?: throw Exception("No hay usuario autenticado")

            Log.d(TAG, "Usuario actual: $usuarioId")

            // Llama al repository para generar la factura
            val resultado = repository.generarFactura(huespedId, usuarioId)

            // Si funciona
            resultado.onSuccess { factura ->
                Log.d(TAG, "Factura generada exitosamente: ${factura.id}")
                emit(factura)
            }

            // Si falla
            resultado.onFailure { exception ->
                Log.e(TAG, "Error al generar factura", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en generarFactura", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Obtiene una factura para visualizarla
     */
    fun obtenerFactura(facturaId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo factura: $facturaId")
            emit("cargando")

            val resultado = repository.obtenerFacturaPorId(facturaId)

            resultado.onSuccess { factura ->
                Log.d(TAG, "Factura obtenida")
                emit(factura)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al obtener factura", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en obtenerFactura", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Obtiene todas las facturas de un huésped
     */
    fun obtenerFacturasPorHuesped(huespedId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo facturas del huésped: $huespedId")
            emit("cargando")

            val resultado = repository.obtenerFacturasPorHuesped(huespedId)

            resultado.onSuccess { facturas ->
                Log.d(TAG, "Se obtuvieron ${facturas.size} facturas")
                emit(facturas)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al obtener facturas", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en obtenerFacturasPorHuesped", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Marca una factura como pagada
     */
    fun marcarComoPagada(facturaId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Marcando factura como pagada: $facturaId")
            emit("cargando")

            val resultado = repository.marcarComoPagada(facturaId)

            resultado.onSuccess {
                Log.d(TAG, "Factura marcada como pagada")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error al marcar como pagada", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception en marcarComoPagada", e)
            emit("error: ${e.message}")
        }
    }
}