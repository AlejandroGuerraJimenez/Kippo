package es.ulpgc.kippo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.ulpgc.kippo.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _hasHousehold = MutableStateFlow<Boolean?>(null)
    val hasHousehold = _hasHousehold.asStateFlow()

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
                _hasHousehold.value = user?.current_household_id != null
            }
            .launchIn(viewModelScope)
    }

    fun signOut() {
        auth.signOut()
    }
}
