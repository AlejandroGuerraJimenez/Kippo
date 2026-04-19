package es.ulpgc.kippo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import es.ulpgc.kippo.model.Household
import es.ulpgc.kippo.model.User
import es.ulpgc.kippo.model.Reward
import es.ulpgc.kippo.repository.HouseholdRepository
import es.ulpgc.kippo.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val householdRepository: HouseholdRepository = HouseholdRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _hasHousehold = MutableStateFlow<Boolean?>(null)
    val hasHousehold = _hasHousehold.asStateFlow()

    private val _household = MutableStateFlow<Household?>(null)
    val household = _household.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members = _members.asStateFlow()

    private val _customRewards = MutableStateFlow<List<Reward>>(emptyList())
    val customRewards = _customRewards.asStateFlow()

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
                val newHouseholdId = user?.current_household_id
                
                if (newHouseholdId != currentHouseholdId) {
                    currentHouseholdId = newHouseholdId
                    if (!newHouseholdId.isNullOrBlank()) {
                        observeCustomRewards(newHouseholdId)
                        refreshHouseholdData()
                    } else {
                        _customRewards.value = emptyList()
                        _household.value = null
                        _members.value = emptyList()
                    }
                }
                
                _hasHousehold.value = !currentHouseholdId.isNullOrBlank()
                _errorMessage.value = null
            }
            .launchIn(viewModelScope)
    }

    private fun observeCustomRewards(householdId: String) {
        firestore.collection("household").document(householdId).collection("rewards")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val rewards = snapshot?.toObjects(Reward::class.java) ?: emptyList()
                _customRewards.value = rewards
            }
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

    fun createCustomReward(title: String, description: String, cost: Long) {
        val hid = currentHouseholdId ?: return
        val newReward = Reward(
            title = title,
            description = description,
            cost = cost,
            householdId = hid,
            icon = "redeem"
        )
        viewModelScope.launch {
            try {
                firestore.collection("household").document(hid).collection("rewards")
                    .add(newReward).await()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create reward: ${e.message}"
            }
        }
    }

    fun purchaseReward(reward: Reward) {
        val user = _currentUserProfile.value ?: return
        if (user.total_points < reward.cost) return

        viewModelScope.launch {
            try {
                val userRef = firestore.collection("users").document(user.uid)
                firestore.runTransaction { transaction ->
                    transaction.update(userRef, "total_points", FieldValue.increment(-reward.cost))
                    transaction.update(userRef, "purchased_rewards", FieldValue.arrayUnion(reward.title))
                }.await()
            } catch (e: Exception) {
                _errorMessage.value = "Purchase failed: ${e.message}"
            }
        }
    }

    fun updateProfile(name: String, username: String, profileIconBase64: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _profileUpdateInProgress.value = true
            val avatar = profileIconBase64 ?: _currentUserProfile.value?.profileicon ?: "placeholder_avatar"
            userRepository.updateUserProfile(uid, name, username, avatar)
            _profileUpdateInProgress.value = false
        }
    }

    fun updateHouseholdName(newName: String) {
        val hid = currentHouseholdId ?: return
        viewModelScope.launch {
            householdRepository.updateHouseholdName(hid, newName)
            refreshHouseholdData()
        }
    }

    fun updateHouseholdImage(imageBase64: String) {
        val hid = currentHouseholdId ?: return
        viewModelScope.launch {
            householdRepository.updateHouseholdImage(hid, imageBase64)
            refreshHouseholdData()
        }
    }

    fun removeMember(userId: String) {
        val hid = currentHouseholdId ?: return
        viewModelScope.launch {
            householdRepository.removeMember(hid, userId)
            refreshHouseholdData()
        }
    }

    fun leaveHousehold() {
        val uid = auth.currentUser?.uid ?: return
        val hid = currentHouseholdId ?: return
        viewModelScope.launch {
            householdRepository.leaveHousehold(uid, hid)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
