package es.ulpgc.kippo.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import es.ulpgc.kippo.repository.AuthRepository

class RegisterViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    // Input states
    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    // UI states
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null) // global error message
    val error: State<String?> = _error

    private val _fieldErrors = mutableStateOf<Map<String, String>>(emptyMap())
    val fieldErrors: State<Map<String, String>> = _fieldErrors

    private val _isSuccess = mutableStateOf(false)
    val isSuccess: State<Boolean> = _isSuccess


    // Input handlers
    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onUsernameChange(value: String) { _username.value = value }
    fun onNameChange(value: String) { _name.value = value }

    private fun validateInputs(): Boolean {
        val errors = mutableMapOf<String, String>()

        val emailValue = _email.value.trim()
        val passwordValue = _password.value
        val usernameValue = _username.value.trim()
        val nameValue = _name.value.trim()

        if (emailValue.isEmpty()) errors["email"] = "El correo es obligatorio"
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) errors["email"] = "Correo no válido"

        if (passwordValue.isEmpty()) errors["password"] = "La contraseña es obligatoria"
        else if (passwordValue.length < 6) errors["password"] = "La contraseña debe tener al menos 6 caracteres"

        // For now, username and name are optional because we only register in Firebase Authentication.
        // Firestore user document (with name/username) will be created later if desired.
        // if (usernameValue.isEmpty()) errors["username"] = "El nombre de usuario es obligatorio"
        // if (nameValue.isEmpty()) errors["name"] = "El nombre es obligatorio"

        _fieldErrors.value = errors
        _error.value = null

        return errors.isEmpty()
    }

    fun onRegisterClick() {
        if (!validateInputs()) return

        _isLoading.value = true
        _error.value = null

        repository.register(_email.value.trim(), _password.value) { success, errorMessage ->
            _isLoading.value = false
            if (success) {
                _isSuccess.value = true
                _password.value = "" // clear password after success
            } else {
                _isSuccess.value = false
                _error.value = errorMessage ?: "Error durante el registro"
            }
        }
    }

    // Removed testRegister and internal debug logs for production
}