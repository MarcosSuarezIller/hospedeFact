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
     * Inicia el proceso de generación de una factura consolidada para un huésped.
     * Consolida todos los pedidos pendientes en un único documento legal.
     * Requiere que el usuario esté autenticado para registrar quién emite la factura.
     *
     * @param huespedId Identificador único del huésped.
     * @return [LiveData] que emite el objeto [Factura] generado o un mensaje de error.
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
     * Recupera los detalles de una factura específica para su visualización.
     * 
     * @param facturaId ID único de la factura a recuperar.
     * @return [LiveData] con el objeto [Factura] o estado de error.
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
     * Obtiene el listado completo de facturas emitidas a nombre de un huésped particular.
     * 
     * @param huespedId ID del huésped.
     * @return [LiveData] con la lista de facturas encontradas.
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
     * Cambia el estado administrativo de una factura a "pagada".
     * 
     * @param facturaId ID de la factura a actualizar.
     * @return [LiveData] con el resultado del cambio de estado.
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