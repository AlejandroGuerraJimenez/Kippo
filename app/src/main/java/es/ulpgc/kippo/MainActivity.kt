package es.ulpgc.kippo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.ui.*
import es.ulpgc.kippo.viewmodel.HomeViewModel
import es.ulpgc.kippo.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val auth = FirebaseAuth.getInstance()
                    val initial = if (auth.currentUser != null) "home_dispatch" else "register"
                    val screenState = remember { mutableStateOf(initial) }
                    var showCreateTaskDialog by remember { mutableStateOf(false) }

                    DisposableEffect(Unit) {
                        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                            val user = firebaseAuth.currentUser
                            if (user == null) {
                                screenState.value = "login"
                            }
                        }
                        auth.addAuthStateListener(listener)
                        onDispose {
                            auth.removeAuthStateListener(listener)
                        }
                    }

                    val homeVm: HomeViewModel = viewModel(key = "home_vm_${auth.currentUser?.uid ?: "none"}")
                    val taskVm: TaskViewModel = viewModel()
                    val household by homeVm.household.collectAsState()
                    val members by homeVm.members.collectAsState()

                    if (showCreateTaskDialog && household != null) {
                        CreateTaskDialog(
                            members = members,
                            onDismiss = { showCreateTaskDialog = false },
                            onCreate = { title, desc, pts, assignedTo ->
                                taskVm.createTask(title, desc, pts, household!!.id, assignedTo)
                            }
                        )
                    }

                    when (screenState.value) {
                        "register" -> RegisterScreen(
                            onRegisterSuccess = { screenState.value = "home_dispatch" },
                            onNavigateToLogin = { screenState.value = "login" }
                        )
                        "login" -> LoginScreen(
                            onLoginSuccess = { screenState.value = "home_dispatch" },
                            onNavigateToRegister = { screenState.value = "register" }
                        )
                        "home_dispatch" -> HomeDispatch(
                            viewModel = homeVm,
                            onSignOut = {
                                auth.signOut()
                                screenState.value = "login"
                            },
                            onCreateHouseholdRequested = { screenState.value = "create_household" },
                            onJoinHouseholdRequested = { screenState.value = "join_household" },
                            onNavigateToTasks = { screenState.value = "tasks" },
                            onCreateTaskRequested = { showCreateTaskDialog = true }
                        )
                        "create_household" -> CreateHouseholdScreen(
                            onHouseholdCreated = { screenState.value = "home_dispatch" }
                        )
                        "join_household" -> JoinHouseholdScreen(
                            onHouseholdJoined = { screenState.value = "home_dispatch" },
                            onBack = { screenState.value = "home_dispatch" }
                        )
                        "tasks" -> {
                            TasksScreen(
                                householdId = household?.id ?: "",
                                members = members,
                                onBack = { screenState.value = "home_dispatch" },
                                onNavigateHome = { screenState.value = "home_dispatch" },
                                onNavigateProfile = { screenState.value = "home_dispatch" },
                                onCreateTaskClick = { showCreateTaskDialog = true },
                                viewModel = taskVm
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeDispatch(
    viewModel: HomeViewModel,
    onSignOut: () -> Unit,
    onCreateHouseholdRequested: () -> Unit,
    onJoinHouseholdRequested: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onCreateTaskRequested: () -> Unit
) {
    val hasHousehold by viewModel.hasHousehold.collectAsState()
    val household by viewModel.household.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val leaveInProgress by viewModel.leaveInProgress.collectAsState()
    val profileUpdateInProgress by viewModel.profileUpdateInProgress.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    when (hasHousehold) {
        true -> HomeScreen(
            onSignOut = onSignOut,
            onLeaveHousehold = { viewModel.leaveHousehold() },
            householdName = household?.name.orEmpty(),
            householdCode = household?.joinCode.orEmpty(),
            profileName = currentUserProfile?.name.orEmpty(),
            profileUsername = currentUserProfile?.username.orEmpty(),
            profileEmail = currentUserProfile?.email.orEmpty(),
            profilePoints = currentUserProfile?.total_points ?: 0L,
            profileIconKey = currentUserProfile?.profileicon.orEmpty(),
            leaveInProgress = leaveInProgress,
            profileUpdateInProgress = profileUpdateInProgress,
            errorMessage = errorMessage,
            onDismissError = { viewModel.clearError() },
            onEditProfile = { name, username -> viewModel.updateProfile(name, username) },
            onNavigateToTasks = onNavigateToTasks,
            onCreateTaskClick = onCreateTaskRequested,
            viewModel = viewModel
        )
        false -> SetupHouseholdScreen(
            onCreateHouseholdClick = onCreateHouseholdRequested,
            onJoinHouseholdClick = onJoinHouseholdRequested,
            onSignOut = onSignOut
        )
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KippoColors.Teal)
            }
        }
    }
}
