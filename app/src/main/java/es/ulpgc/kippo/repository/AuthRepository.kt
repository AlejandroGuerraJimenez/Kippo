package es.ulpgc.kippo.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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
                        onComplete(false, "Could not get user UID")
                        return@addOnCompleteListener
                    }

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
                            val msg = ex.localizedMessage ?: "Error saving user to Firestore"
                            onComplete(false, msg)
                        }
                } else {
                    val ex = task.exception
                    val message = when (ex) {
                        is FirebaseAuthException -> mapErrorCode(ex.errorCode)
                        else -> ex?.localizedMessage ?: "Unknown error during registration"
                    }
                    onComplete(false, message)
                }
            }
            .addOnFailureListener { ex ->
                val msg = ex.localizedMessage ?: "Unknown error in register"
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
                        else -> ex?.localizedMessage ?: "Unknown error during login"
                    }
                    onComplete(false, message)
                }
            }
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    private fun mapErrorCode(code: String): String {
        return when (code) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
            "ERROR_INVALID_EMAIL" -> "Invalid email format"
            "ERROR_WEAK_PASSWORD" -> "Password too weak (minimum 6 characters)"
            "ERROR_USER_NOT_FOUND" -> "User not found"
            "ERROR_WRONG_PASSWORD" -> "Incorrect password"
            else -> "Authentication error: $code"
        }
    }
}
