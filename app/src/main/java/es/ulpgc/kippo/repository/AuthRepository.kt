package es.ulpgc.kippo.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Repositorio para operaciones de autenticación en Firebase.
 * Ahora también crea el documento del usuario en Firestore en la colección `users`.
 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Registra en Firebase Authentication y crea el documento en Firestore /users/{uid}.
     * name y username son opcionales y se incluyen en el documento.
     */
    fun register(
        email: String,
        password: String,
        name: String = "",
        username: String = "",
        onComplete: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        onComplete(false, "No se obtuvo el uid del usuario")
                        return@addOnCompleteListener
                    }

                    // Construir el mapa de datos para Firestore
                    val userMap = hashMapOf<String, Any>(
                        "uid" to uid,
                        "created_at" to FieldValue.serverTimestamp(),
                        "current_household_id" to "",
                        "email" to email,
                        "name" to name,
                        "profileicon" to "placeholder_avatar",
                        "total_points" to 0L,
                        "username" to username
                    )

                    firestore.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            onComplete(true, null)
                        }
                        .addOnFailureListener { ex ->
                            val msg = ex.localizedMessage ?: "Error al guardar usuario en Firestore"
                            onComplete(false, msg)
                        }
                } else {
                    val ex = task.exception
                    val message = when (ex) {
                        is FirebaseAuthException -> mapErrorCode(ex.errorCode)
                        else -> ex?.localizedMessage ?: "Error desconocido durante el registro"
                    }
                    onComplete(false, message)
                }
            }
            .addOnFailureListener { ex ->
                val msg = ex.localizedMessage ?: "Error desconocido en register"
                onComplete(false, msg)
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

