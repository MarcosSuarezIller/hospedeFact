package com.example.hospedefact.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Repository para manejar autenticación con Firebase
 * Se conecta a Firebase Authentication
 */
class AuthRepository {

    // Instancia de Firebase Authentication
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Intenta hacer login con email y contraseña
     *
     * @param email Email del usuario
     * @param password Contraseña
     * @return Result.success(uid) si funciona, Result.failure(exception) si falla
     */
    suspend fun login(email: String, password: String): Result<String> = try {
        // Firebase Auth intenta autenticar
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()

        // Si funciona, devuelve el UID del usuario
        val uid = result.user?.uid ?: ""
        Result.success(uid)

    } catch (e: Exception) {
        // Si falla, devuelve el error
        Result.failure(e)
    }

    /**
     * Crea una nueva cuenta de usuario
     *
     * @param email Email del nuevo usuario
     * @param password Contraseña
     * @return Result.success(uid) si funciona, Result.failure(exception) si falla
     */
    suspend fun registro(email: String, password: String): Result<String> = try {
        // Crea usuario en Firebase Auth
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

        // Si funciona, devuelve el UID
        val uid = result.user?.uid ?: ""
        Result.success(uid)

    } catch (e: Exception) {
        // Si falla, devuelve el error
        Result.failure(e)
    }

    /**
     * Cierra la sesión del usuario actual
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Obtiene el UID del usuario actualmente logueado
     *
     * @return UID si hay usuario logueado, null si no
     */
    fun obtenerUsuarioActual(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Verifica si hay usuario logueado
     */
    fun estaLogueado(): Boolean {
        return firebaseAuth.currentUser != null
    }
}