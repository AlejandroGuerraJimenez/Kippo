package es.ulpgc.kippo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.ulpgc.kippo.repository.HouseholdRepository
import es.ulpgc.kippo.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val householdRepository: HouseholdRepository = HouseholdRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _hasHousehold = MutableStateFlow<Boolean?>(null)
    val hasHousehold = _hasHousehold.asStateFlow()

    private var currentHouseholdId: String? = null

    init {
        observeUserHousehold()
    }

    private fun observeUserHousehold() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _hasHousehold.value = false
            return
        }

        userRepository.observeUser(currentUser.uid)
            .onEach { user ->
                currentHouseholdId = user?.current_household_id
                _hasHousehold.value = currentHouseholdId != null
            }
            .launchIn(viewModelScope)
    }

    fun leaveHousehold() {
        val userId = auth.currentUser?.uid
        val householdId = currentHouseholdId

        if (userId != null && householdId != null) {
            viewModelScope.launch {
                householdRepository.leaveHousehold(userId, householdId)
                // El observador detectará el cambio y actualizará _hasHousehold automáticamente
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
