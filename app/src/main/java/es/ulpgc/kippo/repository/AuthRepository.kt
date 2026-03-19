package es.ulpgc.kippo.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

/**
 * Repositorio para operaciones de autenticación en Firebase.
 * Proporciona funciones para registrar y loguear usuarios y devuelve
 * mensajes de error amigables cuando sea posible.
 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(email: String, password: String, onComplete: (success: Boolean, errorMessage: String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    val ex = task.exception
                    val message = when (ex) {
                        is FirebaseAuthException -> mapErrorCode(ex.errorCode)
                        else -> ex?.localizedMessage ?: "Error desconocido durante el registro"
                    }
                    onComplete(false, message)
                }
            }
    }

    fun login(email: String, password: String, onComplete: (success: Boolean, errorMessage: String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    val ex = task.exception
                    val message = when (ex) {
                        is FirebaseAuthException -> mapErrorCode(ex.errorCode)
                        else -> ex?.localizedMessage ?: "Error desconocido durante el inicio de sesión"
                    }
                    onComplete(false, message)
                }
            }
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    private fun mapErrorCode(code: String): String {
        return when (code) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "El correo ya está en uso"
            "ERROR_INVALID_EMAIL" -> "El correo electrónico no tiene un formato válido"
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil (mínimo 6 caracteres)"
            "ERROR_USER_NOT_FOUND" -> "Usuario no encontrado"
            "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
            else -> "Error de autenticación: $code"
        }
    }
}

