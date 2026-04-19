package es.ulpgc.kippo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.R
import es.ulpgc.kippo.model.Task
import es.ulpgc.kippo.model.User
import es.ulpgc.kippo.viewmodel.HomeViewModel
import es.ulpgc.kippo.ui.components.BottomNavDestination
import es.ulpgc.kippo.ui.components.KippoBottomBar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit = {},
    onLeaveHousehold: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToGastos: () -> Unit = {},
    onNavigateToGroceries: () -> Unit = {},
    onNavigateToRewards: () -> Unit = {},
    pendingTaskDates: Set<LocalDate> = emptySet(),
    onCreateTaskRequested: () -> Unit = {},
    onNavigateToHouseholdProfile: () -> Unit = {},
    currentSectionState: MutableState<es.ulpgc.kippo.ui.components.BottomNavDestination>,
    profileName: String = "",
    profileUsername: String = "",
    profileEmail: String = "",
    profilePoints: Long = 0,
    profileIconKey: String = "placeholder_avatar",
    profileUpdateInProgress: Boolean = false,
    purchasedRewards: List<String> = emptyList(),
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    onEditProfile: (String, String) -> Unit = { _, _ -> },
    allTasks: List<Task> = emptyList(),
    currentUserId: String = "",
    viewModel: HomeViewModel = viewModel()
) {
    val household by viewModel.household.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val userCompletedTasks = remember(allTasks, currentUserId) {
        allTasks.filter { it.completed && it.completedBy == currentUserId }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = profileName,
            initialUsername = profileUsername,
            isSaving = profileUpdateInProgress,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, newUsername ->
                onEditProfile(newName, newUsername)
                if (!profileUpdateInProgress) {
                    showEditProfileDialog = false
                }
            }
        )
    }

    Scaffold(
        topBar = { 
            KippoTopBar(
                householdName = household?.name ?: "Kippo",
                onProfileClick = onNavigateToHouseholdProfile
            ) 
        },
        bottomBar = {
            KippoBottomBar(
                selectedDestination = currentSectionState.value,
                onHomeClick = { currentSectionState.value = es.ulpgc.kippo.ui.components.BottomNavDestination.HOME },
                onTasksClick = onNavigateToTasks,
                onCreateClick = onCreateTaskRequested,
                onGastosClick = onNavigateToGastos,
                onProfileClick = { currentSectionState.value = es.ulpgc.kippo.ui.components.BottomNavDestination.PROFILE }
            )
        },
        containerColor = KippoColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentSectionState.value == es.ulpgc.kippo.ui.components.BottomNavDestination.HOME) {
                ActionButtonsRow(
                    onTasksClick = onNavigateToTasks, 
                    onGastosClick = onNavigateToGastos,
                    onGroceriesClick = onNavigateToGroceries,
                    onRewardsClick = onNavigateToRewards
                )
                Spacer(modifier = Modifier.height(20.dp))
                CalendarWidget(pendingDates = pendingTaskDates)
            } else if (currentSectionState.value == es.ulpgc.kippo.ui.components.BottomNavDestination.PROFILE) {
                ProfileSection(
                    name = profileName,
                    username = profileUsername,
                    email = profileEmail,
                    points = profilePoints,
                    profileIconKey = profileIconKey,
                    completedTasks = userCompletedTasks,
                    purchasedRewards = purchasedRewards,
                    onEditProfile = { showEditProfileDialog = true },
                    onSignOut = onSignOut
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable { onDismissError() }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ActionButtonsRow(
    onTasksClick: () -> Unit, 
    onGastosClick: () -> Unit = {},
    onGroceriesClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onTasksClick,
                modifier = Modifier.weight(1f).height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.TaskAlt, null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("TASKS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = onGastosClick,
                modifier = Modifier.weight(1f).height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.DarkTeal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("EXPENSES", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onGroceriesClick,
                modifier = Modifier.weight(1f).height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Yellow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, null, tint = KippoColors.DarkText, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("GROCERY", color = KippoColors.DarkText, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }

            Button(
                onClick = onRewardsClick,
                modifier = Modifier.weight(1f).height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, KippoColors.Yellow)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, null, tint = KippoColors.Yellow, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("REWARDS", color = KippoColors.DarkText, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePickerSheet(
    onDismiss: () -> Unit,
    onSelectTask: () -> Unit,
    onSelectExpense: () -> Unit,
    onSelectGrocery: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("What do you want to add?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = KippoColors.DarkText, modifier = Modifier.padding(bottom = 4.dp))

            PickerCard("New Task", "Assign a task to the household", KippoColors.Teal, Icons.Default.TaskAlt) { onSelectTask(); onDismiss() }
            PickerCard("New Expense", "Register a shared expense", KippoColors.DarkTeal, Icons.Default.AccountBalanceWallet) { onSelectExpense(); onDismiss() }
            PickerCard("Grocery List", "Create a new grocery list", KippoColors.Yellow, Icons.Default.ShoppingCart) { onSelectGrocery(); onDismiss() }
        }
    }
}

@Composable
fun PickerCard(title: String, desc: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(48.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = KippoColors.DarkText, fontSize = 15.sp)
                Text(desc, color = KippoColors.DarkText.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = color)
        }
    }
}

@Composable
fun CalendarWidget(pendingDates: Set<LocalDate> = emptySet()) {
    val today = remember { LocalDate.now() }
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    val monthName = displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).replaceFirstChar { it.uppercase() }
    val firstDayOfMonth = displayedMonth.atDay(1)
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1)
    val daysInMonth = displayedMonth.lengthOfMonth()
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ChevronLeft, null, tint = KippoColors.Teal) }
                Text(text = "$monthName ${displayedMonth.year}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = KippoColors.DarkText, fontSize = 15.sp)
                IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ChevronRight, null, tint = KippoColors.Teal) }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) { dayLabels.forEach { Text(text = it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = KippoColors.DarkText.copy(alpha = 0.4f)) } }
            Spacer(Modifier.height(4.dp))
            val rows = (startOffset + daysInMonth + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val day = row * 7 + col - startOffset + 1
                        val date = if (day in 1..daysInMonth) displayedMonth.atDay(day) else null
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                            if (date != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.size(28.dp).then(if (date == today) Modifier.background(KippoColors.Teal, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
                                        Text(text = day.toString(), fontSize = 13.sp, color = if (date == today) Color.White else KippoColors.DarkText)
                                    }
                                    if (date in pendingDates) Box(modifier = Modifier.size(4.dp).background(if (date < today) Color(0xFFE53935) else KippoColors.Teal, CircleShape))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KippoTopBar(householdName: String, onProfileClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(52.dp).clickable { onProfileClick() }, shadowElevation = 2.dp) {
            Image(painter = painterResource(id = R.drawable.ic_logo_kippo), contentDescription = "Logo", modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f).clickable { onProfileClick() }) {
            Text(text = householdName, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = KippoColors.DarkText)
            Text(text = "Kippo Household", fontSize = 13.sp, color = KippoColors.DarkText.copy(alpha = 0.5f))
        }
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(Icons.Outlined.Notifications, null, tint = KippoColors.DarkText, modifier = Modifier.size(28.dp))
            Surface(color = Color(0xFFE57373), shape = CircleShape, modifier = Modifier.size(10.dp).offset(x = (-2).dp, y = (2).dp), border = BorderStroke(1.5.dp, KippoColors.Background)) {}
        }
    }
}
