package es.ulpgc.kippo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.R
import es.ulpgc.kippo.model.User
import es.ulpgc.kippo.viewmodel.HomeViewModel
import es.ulpgc.kippo.ui.components.BottomNavDestination
import es.ulpgc.kippo.ui.components.KippoBottomBar

enum class HomeSection {
    HOME,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit = {},
    onLeaveHousehold: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    householdName: String = "",
    householdCode: String = "",
    profileName: String = "",
    profileUsername: String = "",
    profileEmail: String = "",
    profilePoints: Long = 0,
    profileIconKey: String = "placeholder_avatar",
    leaveInProgress: Boolean = false,
    profileUpdateInProgress: Boolean = false,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    onEditProfile: (String, String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = viewModel()
) {
    val household by viewModel.household.collectAsState()
    val members by viewModel.members.collectAsState()
    val leaveInProgress by viewModel.leaveInProgress.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showLeaveDialog by remember { mutableStateOf(false) }
    var currentSection by remember { mutableStateOf(HomeSection.HOME) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Household") },
            text = { Text("Are you sure you want to leave '${household?.name}'? You will need an invitation code to join again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        onLeaveHousehold()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLeaveDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = KippoColors.DarkTeal)
                ) {
                    Text("Cancel")
                }
            }
        )
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
        topBar = { KippoTopBar(householdName = household?.name ?: "Home") },
        bottomBar = {
            KippoBottomBar(
                selectedDestination = if (currentSection == HomeSection.PROFILE) {
                    BottomNavDestination.PROFILE
                } else {
                    BottomNavDestination.HOME
                },
                onHomeClick = { currentSection = HomeSection.HOME },
                onTasksClick = onNavigateToTasks,
                onCreateClick = onCreateTaskClick,
                onProfileClick = { currentSection = HomeSection.PROFILE }
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
            if (currentSection == HomeSection.HOME) {
                MembersSection(members)
                Spacer(modifier = Modifier.height(16.dp))
                ActionButtonsRow(onTasksClick = onNavigateToTasks)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentSection == HomeSection.HOME) {
                household?.let { h ->
                    InviteCodeCard(h.joinCode)
                }
            } else {
                ProfileSection(
                    name = profileName,
                    username = profileUsername,
                    email = profileEmail,
                    points = profilePoints,
                    profileIconKey = profileIconKey,
                    onEditProfile = { showEditProfileDialog = true },
                    onSignOut = onSignOut
                )
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (currentSection == HomeSection.HOME) {
                OutlinedButton(
                    onClick = { showLeaveDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !leaveInProgress
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Leave Household")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MembersSection(members: List<User>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Household Members",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KippoColors.DarkText
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(members) { member ->
                MemberItem(member)
            }
        }
    }
}

@Composable
fun MemberItem(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(KippoColors.Teal.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = KippoColors.Teal,
                modifier = Modifier.size(35.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (user.username.isNotBlank()) user.username else "User",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = KippoColors.DarkText,
            maxLines = 1
        )
    }
}

@Composable
fun InviteCodeCard(code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Household Invite Code",
                style = MaterialTheme.typography.titleSmall,
                color = KippoColors.DarkText.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = code,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = KippoColors.Teal,
                letterSpacing = 4.sp
            )
        }
    }
}

@Composable
fun ActionButtonsRow(onTasksClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onTasksClick,
            modifier = Modifier
                .weight(1f)
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.TaskAlt,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "TASKS",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }

        Button(
            onClick = { /* Navigate to Rewards */ },
            modifier = Modifier
                .weight(1f)
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Yellow),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = KippoColors.DarkText,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "REWARDS",
                    color = KippoColors.DarkText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Profile UI extracted to ProfileScreen.kt

@Composable
fun KippoTopBar(householdName: String = "Home") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(52.dp),
            shadowElevation = 2.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_kippo),
                contentDescription = "Logo",
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Kippo",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = KippoColors.DarkText
            )
            Text(
                text = householdName,
                fontSize = 15.sp,
                color = KippoColors.DarkText.copy(alpha = 0.6f)
            )
        }

        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = KippoColors.DarkText,
                modifier = Modifier.size(28.dp)
            )
            Surface(
                color = Color(0xFFE57373),
                shape = CircleShape,
                modifier = Modifier
                    .size(10.dp)
                    .offset(x = (-2).dp, y = (2).dp),
                border = BorderStroke(1.5.dp, KippoColors.Background)
            ) {}
        }
    }
}

// Bottom bar extracted to ui/components/KippoBottomBar.kt for reuse.
