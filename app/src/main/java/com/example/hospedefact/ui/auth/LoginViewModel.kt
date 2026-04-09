package com.example.hospedefact.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.liveData
import com.example.hospedefact.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Login
 * Maneja la lógica de autenticación
 */

class LoginViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    /**
     * Intenta hacer login
     * Devuelve LiveData que emite:
     * - "cargando" mientras se procesa
     * - "exito" si login funcionó
     * - "error: mensaje" si falló
     */
    fun login(email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            // Emite "cargando" mientras procesa
            emit("cargando")

            // Llama al repository para hacer login
            val resultado = authRepository.login(email, password)

            // Si funciona
            resultado.onSuccess { uid ->
                android.util.Log.d("LoginViewModel", "Login exitoso. UID: $uid")
                emit("exito")
            }

            // Si falla
            resultado.onFailure { exception ->
                val mensajeError = exception.message ?: "Error desconocido"
                android.util.Log.e("LoginViewModel", "Error en login: $mensajeError")
                emit("error: $mensajeError")
            }

        } catch (e: Exception) {
            android.util.Log.e("LoginViewModel", "Exception en login", e)
            emit("error: ${e.message}")
        }
    }

    /**
     * Registra un nuevo usuario
     * (Usar en el futuro)
     */
    fun registro(email: String, password: String) = liveData(Dispatchers.IO) {
        try {
            emit("cargando")

            val resultado = authRepository.registro(email, password)

            resultado.onSuccess { uid ->
                android.util.Log.d("LoginViewModel", "Registro exitoso. UID: $uid")
                emit("exito")
            }

            resultado.onFailure { exception ->
                val mensajeError = exception.message ?: "Error desconocido"
                android.util.Log.e("LoginViewModel", "Error en registro: $mensajeError")
                emit("error: $mensajeError")
            }

        } catch (e: Exception) {
            android.util.Log.e("LoginViewModel", "Exception en registro", e)
            emit("error: ${e.message}")
        }
    }
}
