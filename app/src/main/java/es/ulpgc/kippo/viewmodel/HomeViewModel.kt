package es.ulpgc.kippo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.ulpgc.kippo.model.Household
import es.ulpgc.kippo.model.User
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

    private val _household = MutableStateFlow<Household?>(null)
    val household = _household.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members = _members.asStateFlow()

    private val _leaveInProgress = MutableStateFlow(false)
    val leaveInProgress = _leaveInProgress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _profileUpdateInProgress = MutableStateFlow(false)
    val profileUpdateInProgress = _profileUpdateInProgress.asStateFlow()

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
                _currentUserProfile.value = user
                currentHouseholdId = user?.current_household_id
                _hasHousehold.value = !currentHouseholdId.isNullOrBlank()
                _errorMessage.value = null

                if (!currentHouseholdId.isNullOrBlank()) {
                    refreshHouseholdData()
                } else {
                    _household.value = null
                    _members.value = emptyList()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun refreshHouseholdData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val householdResult = householdRepository.getUserHousehold(uid)
            val householdData = householdResult.getOrNull()
            _household.value = householdData
            
            householdData?.let { 
                fetchMembers(it.members)
            }
        }
    }

    private fun fetchMembers(uids: List<String>) {
        viewModelScope.launch {
            val result = userRepository.getUsers(uids)
            result.onSuccess { userList ->
                _members.value = userList
            }.onFailure {
                _members.value = emptyList()
            }
        }
    }

    fun updateHouseholdName(newName: String) {
        val hid = currentHouseholdId ?: return
        if (newName.isBlank()) return
        
        viewModelScope.launch {
            val result = householdRepository.updateHouseholdName(hid, newName)
            result.onSuccess {
                refreshHouseholdData()
            }.onFailure { ex ->
                _errorMessage.value = ex.message ?: "Could not update name"
            }
        }
    }

    fun removeMember(userIdToRemove: String) {
        val hid = currentHouseholdId ?: return
        viewModelScope.launch {
            val result = householdRepository.removeMember(hid, userIdToRemove)
            result.onSuccess {
                refreshHouseholdData()
            }.onFailure { ex ->
                _errorMessage.value = ex.message ?: "Could not remove member"
            }
        }
    }

    fun leaveHousehold() {
        val userId = auth.currentUser?.uid
        val householdId = currentHouseholdId

        if (userId != null && householdId != null) {
            viewModelScope.launch {
                _leaveInProgress.value = true
                val result = householdRepository.leaveHousehold(userId, householdId)
                result.onFailure { ex ->
                    _errorMessage.value = ex.message ?: "Could not leave household"
                }
                _leaveInProgress.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun updateProfile(name: String, username: String) {
        val uid = auth.currentUser?.uid ?: return
        val cleanName = name.trim()
        val cleanUsername = username.trim()

        if (cleanName.isBlank()) {
            _errorMessage.value = "Name cannot be empty"
            return
        }

        if (cleanUsername.isBlank()) {
            _errorMessage.value = "Username cannot be empty"
            return
        }

        viewModelScope.launch {
            _profileUpdateInProgress.value = true
            val avatar = _currentUserProfile.value?.profileicon?.ifBlank { "placeholder_avatar" }
                ?: "placeholder_avatar"

            val result = userRepository.updateUserProfile(
                uid = uid,
                name = cleanName,
                username = cleanUsername,
                profileIcon = avatar
            )

            result.onFailure { ex ->
                _errorMessage.value = ex.message ?: "Could not update profile"
            }
            _profileUpdateInProgress.value = false
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
