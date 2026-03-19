package es.ulpgc.kippo.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.ulpgc.kippo.model.User

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isSuccess = mutableStateOf(false)
    val isSuccess: State<Boolean> = _isSuccess

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onRegisterClick() {
        val emailValue = _email.value
        val passwordValue = _password.value
        val usernameValue = _username.value

        if (emailValue.isEmpty() || passwordValue.isEmpty() || usernameValue.isEmpty()) {
            _error.value = "Todos los campos son obligatorios"
            return
        }

        _isLoading.value = true
        _error.value = null

        auth.createUserWithEmailAndPassword(emailValue, passwordValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val user = User(uid = uid, email = emailValue, username = usernameValue)
                    
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            _isSuccess.value = true
                        }
                        .addOnFailureListener { e ->
                            _isLoading.value = false
                            _error.value = e.message
                        }
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message
                }
            }
    }
}