package es.ulpgc.kippo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.ulpgc.kippo.repository.HouseholdRepository
import es.ulpgc.kippo.model.Household
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateHouseholdViewModel(
    private val householdRepository: HouseholdRepository = HouseholdRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _householdName = MutableStateFlow("")
    val householdName = _householdName.asStateFlow()

    private val _creationState = MutableStateFlow<CreationState>(CreationState.Idle)
    val creationState = _creationState.asStateFlow()

    fun onHouseholdNameChange(newName: String) {
        _householdName.value = newName
    }

    fun createHousehold() {
        val name = _householdName.value
        val currentUser = auth.currentUser

        if (name.isBlank()) {
            _creationState.value = CreationState.Error("Household name cannot be empty")
            return
        }

        if (currentUser == null) {
            _creationState.value = CreationState.Error("User not authenticated")
            return
        }

        _creationState.value = CreationState.Loading
        viewModelScope.launch {
            val result = householdRepository.createHousehold(name, currentUser.uid)
            result.onSuccess { household ->
                _creationState.value = CreationState.Success(household)
            }.onFailure { e ->
                _creationState.value = CreationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class CreationState {
        object Idle : CreationState()
        object Loading : CreationState()
        data class Success(val household: Household) : CreationState()
        data class Error(val message: String) : CreationState()
    }
}