package es.ulpgc.kippo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.ulpgc.kippo.model.Reward

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    userPoints: Long,
    customRewards: List<Reward> = emptyList(),
    onBack: () -> Unit,
    onPurchase: (Reward) -> Unit,
    onCreateCustomReward: (String, String, Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateRewardDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = onCreateCustomReward
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kippo Rewards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KippoColors.Background
                )
            )
        },
        containerColor = KippoColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Header con puntos actuales
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KippoColors.DarkTeal)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Your Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text(
                            text = "$userPoints PTS",
                            color = KippoColors.Yellow,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = KippoColors.Yellow,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        "Default Rewards",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = KippoColors.DarkText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(Reward.SAMPLE_REWARDS) { reward ->
                    RewardItem(
                        reward = reward, 
                        canAfford = userPoints >= reward.cost,
                        onPurchase = { onPurchase(reward) }
                    )
                }

                if (customRewards.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            "Household Rewards",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = KippoColors.DarkText,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(customRewards) { reward ->
                        RewardItem(
                            reward = reward,
                            canAfford = userPoints >= reward.cost,
                            onPurchase = { onPurchase(reward) }
                        )
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("CREATE CUSTOM REWARD", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRewardDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Custom Reward", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reward Name") },
                    placeholder = { Text("Ex: Week without dishes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { if (it.all { c -> c.isDigit() }) cost = it },
                    label = { Text("Cost (PTS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && cost.isNotBlank()) {
                        onCreate(title, description, cost.toLongOrNull() ?: 0L)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal)
            ) {
                Text("CREATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun RewardItem(reward: Reward, canAfford: Boolean, onPurchase: () -> Unit) {
    val icon = when(reward.icon) {
        "pizza" -> Icons.Default.LocalPizza
        "movie" -> Icons.Default.Movie
        "game" -> Icons.Default.VideogameAsset
        else -> Icons.Default.CardGiftcard
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = if (canAfford) KippoColors.Teal.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (canAfford) KippoColors.Teal else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            
            Text(
                reward.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = KippoColors.DarkText,
                maxLines = 1
            )
            
            Text(
                reward.description,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 14.sp,
                modifier = Modifier.height(30.dp),
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onPurchase,
                enabled = canAfford,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) KippoColors.Yellow else Color.LightGray,
                    contentColor = KippoColors.DarkText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("${reward.cost} PTS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
