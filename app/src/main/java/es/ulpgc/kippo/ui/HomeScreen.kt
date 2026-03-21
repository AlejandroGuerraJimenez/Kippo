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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.R
import es.ulpgc.kippo.model.User
import es.ulpgc.kippo.viewmodel.HomeViewModel
import es.ulpgc.kippo.ui.components.BottomNavDestination
import es.ulpgc.kippo.ui.components.KippoBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit = {},
    onLeaveHousehold: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onNavigateToHouseholdProfile: () -> Unit = {},
    profileName: String = "",
    profileUsername: String = "",
    profileEmail: String = "",
    profilePoints: Long = 0,
    profileIconKey: String = "placeholder_avatar",
    profileUpdateInProgress: Boolean = false,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    onEditProfile: (String, String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = viewModel()
) {
    val household by viewModel.household.collectAsState()
    
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var currentSection by remember { mutableStateOf(BottomNavDestination.HOME) }

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
                selectedDestination = currentSection,
                onHomeClick = { currentSection = BottomNavDestination.HOME },
                onTasksClick = onNavigateToTasks,
                onCreateClick = onCreateTaskClick,
                onProfileClick = { currentSection = BottomNavDestination.PROFILE }
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
            if (currentSection == BottomNavDestination.HOME) {
                ActionButtonsRow(onTasksClick = onNavigateToTasks)
                
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Welcome back!",
                    color = KippoColors.DarkText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "What would you like to do today?",
                    color = KippoColors.DarkText.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else if (currentSection == BottomNavDestination.PROFILE) {
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

@Composable
fun KippoTopBar(householdName: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier
                .size(52.dp)
                .clickable { onProfileClick() },
            shadowElevation = 2.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_kippo),
                contentDescription = "Household Logo",
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() }
        ) {
            Text(
                text = householdName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = KippoColors.DarkText
            )
            Text(
                text = "Kippo Household",
                fontSize = 13.sp,
                color = KippoColors.DarkText.copy(alpha = 0.5f)
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
