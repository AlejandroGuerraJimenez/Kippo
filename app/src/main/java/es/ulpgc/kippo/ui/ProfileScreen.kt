package es.ulpgc.kippo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.ulpgc.kippo.model.Task
import es.ulpgc.kippo.model.Reward

@Composable
fun ProfileSection(
    name: String,
    username: String,
    email: String,
    points: Long,
    profileIconKey: String,
    completedTasks: List<Task> = emptyList(),
    purchasedRewards: List<String> = emptyList(),
    customRewards: List<Reward> = emptyList(),
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    var showPointsHistory by remember { mutableStateOf(false) }

    if (showPointsHistory) {
        PointsHistoryDialog(
            tasks = completedTasks,
            purchasedRewards = purchasedRewards,
            customRewards = customRewards,
            onDismiss = { showPointsHistory = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Badge de puntos en la esquina superior derecha con margen
            Surface(
                color = KippoColors.Yellow,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clickable { showPointsHistory = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = KippoColors.DarkText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = points.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = KippoColors.DarkText
                    )
                    Text(
                        text = "PTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KippoColors.DarkText,
                        fontSize = 10.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = KippoColors.Background,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val avatarTint = if (profileIconKey == "placeholder_avatar") KippoColors.DarkTeal else KippoColors.Teal
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile avatar",
                            tint = avatarTint,
                            modifier = Modifier.size(88.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = name.ifBlank { "No name" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText
                )
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodyLarge,
                    color = KippoColors.DarkText.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = KippoColors.Teal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = email,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = KippoColors.Teal,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (purchasedRewards.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Your Rewards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KippoColors.DarkText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    purchasedRewards.forEach { rewardTitle ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = KippoColors.Yellow.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, KippoColors.Yellow)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = KippoColors.DarkText)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = rewardTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = KippoColors.DarkText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KippoColors.DarkText),
                    border = BorderStroke(1.dp, KippoColors.Yellow.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Edit profile", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KippoColors.DarkTeal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Sign out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PointsHistoryDialog(
    tasks: List<Task>,
    purchasedRewards: List<String> = emptyList(),
    customRewards: List<Reward> = emptyList(),
    onDismiss: () -> Unit
) {
    val allPossibleRewards = Reward.SAMPLE_REWARDS + customRewards

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = KippoColors.Yellow, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Points History", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (tasks.isEmpty() && purchasedRewards.isEmpty()) {
                Text("No activity yet.", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tareas que suman puntos
                    items(tasks) { task ->
                        HistoryItem(
                            title = task.title,
                            points = "+${task.points}",
                            color = KippoColors.Teal,
                            icon = Icons.Default.CheckCircle
                        )
                    }
                    // Compras que restan puntos
                    items(purchasedRewards) { rewardTitle ->
                        // Buscamos el coste real del reward tanto en fijos como en personalizados
                        // Usamos ignoreCase para mayor seguridad al comparar títulos
                        val rewardInfo = allPossibleRewards.find { it.title.equals(rewardTitle, ignoreCase = true) }
                        val cost = rewardInfo?.cost ?: 0
                        HistoryItem(
                            title = "Reward: $rewardTitle",
                            points = if (cost > 0) "-$cost" else "0",
                            color = Color(0xFFE53935),
                            icon = Icons.Default.RemoveCircle
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = KippoColors.Teal, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun HistoryItem(title: String, points: String, color: Color, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KippoColors.Background.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = points,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    initialName: String,
    initialUsername: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KippoColors.Teal,
        unfocusedBorderColor = KippoColors.DarkTeal.copy(alpha = 0.3f),
        focusedLabelColor = KippoColors.Teal,
        unfocusedLabelColor = KippoColors.DarkText.copy(alpha = 0.6f),
        cursorColor = KippoColors.Teal,
        focusedTextColor = KippoColors.DarkText,
        unfocusedTextColor = KippoColors.DarkText
    )

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Edit profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSaving,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSaving,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, username) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isSaving) "Saving..." else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
