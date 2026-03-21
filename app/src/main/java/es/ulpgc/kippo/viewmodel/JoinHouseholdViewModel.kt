package es.ulpgc.kippo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.ulpgc.kippo.model.Household
import es.ulpgc.kippo.repository.HouseholdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JoinHouseholdViewModel(
    private val householdRepository: HouseholdRepository = HouseholdRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _joinCode = MutableStateFlow("")
    val joinCode = _joinCode.asStateFlow()

    private val _joinState = MutableStateFlow<JoinState>(JoinState.Idle)
    val joinState = _joinState.asStateFlow()

    fun onJoinCodeChange(newValue: String) {
        val digitsOnly = newValue.filter { it.isDigit() }.take(6)
        _joinCode.value = digitsOnly
    }

    fun joinHousehold() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _joinState.value = JoinState.Error("Usuario no autenticado")
            return
        }

        if (_joinCode.value.length != 6) {
            _joinState.value = JoinState.Error("Introduce un código de 6 dígitos")
            return
        }

        _joinState.value = JoinState.Loading
        viewModelScope.launch {
            val result = householdRepository.joinHouseholdByCode(currentUser.uid, _joinCode.value)
            result.onSuccess { household ->
                _joinState.value = JoinState.Success(household)
            }.onFailure { ex ->
                _joinState.value = JoinState.Error(ex.message ?: "No se pudo unir al household")
            }
        }
    }

    sealed class JoinState {
        object Idle : JoinState()
        object Loading : JoinState()
        data class Success(val household: Household) : JoinState()
        data class Error(val message: String) : JoinState()
    }
}
