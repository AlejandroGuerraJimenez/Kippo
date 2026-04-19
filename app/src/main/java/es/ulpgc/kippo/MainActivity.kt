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
import java.time.LocalDate
import java.time.ZoneId
import es.ulpgc.kippo.model.Task
import es.ulpgc.kippo.ui.*
import es.ulpgc.kippo.viewmodel.ExpenseViewModel
import es.ulpgc.kippo.viewmodel.GroceryViewModel
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
                    var showAddExpenseDialog by remember { mutableStateOf(false) }
                    var showCreateGroceryDialog by remember { mutableStateOf(false) }
                    var showCreatePicker by remember { mutableStateOf(false) }

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
                    val expenseVm: ExpenseViewModel = viewModel()
                    val groceryVm: GroceryViewModel = viewModel()
                    
                    val household by homeVm.household.collectAsState()
                    // Shared bottom navigation selection state used by HomeScreen and to request opening Profile
                    val currentSectionState = remember { mutableStateOf(es.ulpgc.kippo.ui.components.BottomNavDestination.HOME) }
                    val allTasks by taskVm.tasks.collectAsState()

                    // Observe tasks as soon as household is available so CalendarWidget has data
                    LaunchedEffect(household?.id) {
                        household?.id?.let { taskVm.observeTasks(it) }
                    }

                    val pendingTaskDates = remember(allTasks) {
                        allTasks
                            .filter { !it.completed && it.dueDate != null }
                            .mapNotNull { task ->
                                task.dueDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                            }
                            .toSet()
                    }
                    val members by homeVm.members.collectAsState()

                    if (showCreateTaskDialog && household != null) {
                        CreateTaskDialog(
                            members = members,
                            onDismiss = { showCreateTaskDialog = false },
                            onCreate = { title, desc, pts, assignedTo, recurrence, dueDate ->
                                taskVm.createTask(title, desc, pts, household!!.id, assignedTo, recurrence, dueDate)
                            }
                        )
                    }

                    if (showAddExpenseDialog && household != null) {
                        AddExpenseDialog(
                            members = members,
                            currentUserId = auth.currentUser?.uid ?: "",
                            onDismiss = { showAddExpenseDialog = false },
                            onAdd = { title, amount, paidBy, splitAmong, category, notes, customSplits ->
                                expenseVm.addExpense(title, amount, paidBy, splitAmong, category, notes, customSplits)
                            }
                        )
                    }

                    if (showCreateGroceryDialog && household != null) {
                        CreateGroceryListDialog(
                            onDismiss = { showCreateGroceryDialog = false },
                            onCreate = { name, items ->
                                groceryVm.createGroceryList(name, household!!.id, auth.currentUser?.uid ?: "", items)
                                showCreateGroceryDialog = false
                                screenState.value = "grocery_list"
                            }
                        )
                    }

                    if (showCreatePicker) {
                        CreatePickerSheet(
                            onDismiss = { showCreatePicker = false },
                            onSelectTask = { showCreateTaskDialog = true },
                            onSelectExpense = { showAddExpenseDialog = true },
                            onSelectGrocery = { showCreateGroceryDialog = true }
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
                            onNavigateToGastos = { screenState.value = "gastos" },
                            onNavigateToGroceries = { screenState.value = "grocery_list" },
                            onCreateTaskRequested = { showCreatePicker = true },
                            onNavigateToHouseholdProfile = { screenState.value = "household_profile" },
                            pendingTaskDates = pendingTaskDates,
                            currentSectionState = currentSectionState,
                            allTasks = allTasks,
                            currentUserId = auth.currentUser?.uid ?: ""
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
                                onNavigateProfile = { currentSectionState.value = es.ulpgc.kippo.ui.components.BottomNavDestination.PROFILE; screenState.value = "home_dispatch" },
                                onNavigateToExpenses = { screenState.value = "gastos" },
                                onCreateTaskClick = { showCreatePicker = true },
                                viewModel = taskVm
                            )
                        }
                        "gastos" -> {
                                ExpenseScreen(
                                householdId = household?.id ?: "",
                                members = members,
                                currentUserId = auth.currentUser?.uid ?: "",
                                onNavigateHome = { screenState.value = "home_dispatch" },
                                onNavigateToTasks = { screenState.value = "tasks" },
                                onNavigateToProfile = { currentSectionState.value = es.ulpgc.kippo.ui.components.BottomNavDestination.PROFILE; screenState.value = "home_dispatch" },
                                onAddExpenseClick = { showCreatePicker = true },
                                viewModel = expenseVm
                            )
                        }
                        "household_profile" -> {
                            if (household != null) {
                                HouseholdProfileScreen(
                                    household = household!!,
                                    members = members,
                                    onBack = { screenState.value = "home_dispatch" },
                                    onUpdateName = { homeVm.updateHouseholdName(it) },
                                    onRemoveMember = { homeVm.removeMember(it) }
                                )
                            } else {
                                screenState.value = "home_dispatch"
                            }
                        }
                        "grocery_list" -> {
                            GroceryListScreen(
                                householdId = household?.id ?: "",
                                currentUserId = auth.currentUser?.uid ?: "",
                                onNavigateHome = { screenState.value = "home_dispatch" },
                                onNavigateToTasks = { screenState.value = "tasks" },
                                onNavigateToGastos = { screenState.value = "gastos" },
                                onNavigateProfile = { currentSectionState.value = es.ulpgc.kippo.ui.components.BottomNavDestination.PROFILE; screenState.value = "home_dispatch" },
                                onAddGroceryClick = { showCreatePicker = true },
                                viewModel = groceryVm
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
    onNavigateToGastos: () -> Unit = {},
    onNavigateToGroceries: () -> Unit = {},
    onCreateTaskRequested: () -> Unit,
    onNavigateToHouseholdProfile: () -> Unit,
    pendingTaskDates: Set<LocalDate> = emptySet(),
    currentSectionState: MutableState<es.ulpgc.kippo.ui.components.BottomNavDestination>,
    allTasks: List<Task> = emptyList(),
    currentUserId: String = ""
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
            profileName = currentUserProfile?.name.orEmpty(),
            profileUsername = currentUserProfile?.username.orEmpty(),
            profileEmail = currentUserProfile?.email.orEmpty(),
            profilePoints = currentUserProfile?.total_points ?: 0L,
            profileIconKey = currentUserProfile?.profileicon.orEmpty(),
            profileUpdateInProgress = profileUpdateInProgress,
            onEditProfile = { name, username -> viewModel.updateProfile(name, username) },
            onNavigateToTasks = onNavigateToTasks,
            onNavigateToGastos = onNavigateToGastos,
            onNavigateToGroceries = onNavigateToGroceries,
            onCreateTaskClick = onCreateTaskRequested,
            onNavigateToHouseholdProfile = onNavigateToHouseholdProfile,
            pendingTaskDates = pendingTaskDates,
            currentSectionState = currentSectionState,
            allTasks = allTasks,
            currentUserId = currentUserId,
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
