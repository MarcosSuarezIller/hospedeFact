package com.example.hospedefact.ui.almacen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.hospedefact.data.models.Proveedor
import com.example.hospedefact.data.repository.ProveedorRepository
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel para gestión de Proveedores
 */
class ProveedorViewModel(
    private val repository: ProveedorRepository = ProveedorRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "ProveedorViewModel"
    }

    /**
     * Crea un nuevo proveedor en el sistema tras validar los campos obligatorios.
     * 
     * @param nombre Nombre de la empresa o proveedor.
     * @param contacto Nombre de la persona de contacto.
     * @param email Correo electrónico principal.
     * @param telefono Número de teléfono de contacto.
     * @param direccion Dirección física.
     * @param ciudad Ciudad de ubicación.
     * @param pais País del proveedor.
     * @param codigoPostal Código postal.
     * @param tiempoEntrega Tiempo estimado de entrega en días.
     * @return [LiveData] que emite el estado del proceso ("exito", "cargando" o "error:").
     */
    fun crearProveedor(
        nombre: String,
        contacto: String,
        email: String,
        telefono: String,
        direccion: String,
        ciudad: String,
        pais: String,
        codigoPostal: String,
        tiempoEntrega: Int
    ) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creando proveedor: $nombre")
            emit("cargando")

            if (nombre.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
                emit("error: Completa los campos obligatorios")
                return@liveData
            }

            val proveedor = Proveedor(
                nombre = nombre,
                contacto = contacto,
                email = email,
                telefono = telefono,
                direccion = direccion,
                ciudad = ciudad,
                pais = pais,
                codigoPostal = codigoPostal,
                tiempoEntrega = tiempoEntrega
            )

            val resultado = repository.crearProveedor(proveedor)

            resultado.onSuccess { id ->
                Log.d(TAG, "Proveedor creado: $id")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Inicia la recuperación de la lista completa de proveedores activos.
     * 
     * @return [LiveData] que emite la lista de [Proveedor] o estados de carga/error.
     */
    fun cargarProveedores() = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cargando proveedores")
            emit("cargando")

            val resultado = repository.obtenerProveedores()

            resultado.onSuccess { proveedores ->
                Log.d(TAG, "Se cargaron ${proveedores.size} proveedores")
                emit(proveedores)
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Actualiza la información de un proveedor existente mediante el repositorio.
     * 
     * @param proveedor Objeto [Proveedor] con los datos actualizados.
     * @return [LiveData] con el resultado de la operación.
     */
    fun actualizarProveedor(proveedor: Proveedor) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando proveedor: ${proveedor.nombre}")
            emit("cargando")

            val resultado = repository.actualizarProveedor(proveedor)

            resultado.onSuccess {
                Log.d(TAG, "Proveedor actualizado")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Procesa la baja lógica de un proveedor en el sistema.
     * 
     * @param proveedorId ID único del proveedor a desactivar.
     * @return [LiveData] con el resultado de la desactivación.
     */
    fun desactivarProveedor(proveedorId: String) = liveData(Dispatchers.IO) {
        try {
            Log.d(TAG, "Desactivando proveedor: $proveedorId")
            emit("cargando")

            val resultado = repository.desactivarProveedor(proveedorId)

            resultado.onSuccess {
                Log.d(TAG, "Proveedor desactivado")
                emit("exito")
            }

            resultado.onFailure { exception ->
                Log.e(TAG, "Error", exception)
                emit("error: ${exception.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            emit("error: ${e.message}")
        }
    }
}