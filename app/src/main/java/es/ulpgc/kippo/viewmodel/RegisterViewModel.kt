package es.ulpgc.kippo.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import es.ulpgc.kippo.repository.AuthRepository

class RegisterViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _fieldErrors = mutableStateOf<Map<String, String>>(emptyMap())
    val fieldErrors: State<Map<String, String>> = _fieldErrors

    private val _isSuccess = mutableStateOf(false)
    val isSuccess: State<Boolean> = _isSuccess

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onUsernameChange(value: String) { _username.value = value }
    fun onNameChange(value: String) { _name.value = value }

    fun resetState() {
        _email.value = ""
        _password.value = ""
        _username.value = ""
        _name.value = ""
        _isSuccess.value = false
        _error.value = null
        _fieldErrors.value = emptyMap()
        _isLoading.value = false
    }

    private fun validateInputs(): Boolean {
        val errors = mutableMapOf<String, String>()

        val emailValue = _email.value.trim()
        val passwordValue = _password.value
        if (emailValue.isEmpty()) errors["email"] = "Email is required"
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) errors["email"] = "Invalid email"

        if (passwordValue.isEmpty()) errors["password"] = "Password is required"
        else if (passwordValue.length < 6) errors["password"] = "Password must be at least 6 characters"

        _fieldErrors.value = errors
        _error.value = null

        return errors.isEmpty()
    }

    fun onRegisterClick() {
        if (!validateInputs()) return

        _isLoading.value = true
        _error.value = null

        repository.register(
            _email.value.trim(),
            _password.value,
            name = _name.value.trim(),
            username = _username.value.trim()
        ) { success, errorMessage ->
            _isLoading.value = false
            if (success) {
                _isSuccess.value = true
            } else {
                _isSuccess.value = false
                _error.value = errorMessage ?: "Registration error"
            }
        }
    }
}
