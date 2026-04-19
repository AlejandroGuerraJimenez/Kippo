package es.ulpgc.kippo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.ulpgc.kippo.model.Reward

private val sampleRewards = listOf(
    Reward("1", "Pizza Night", "A delicious pizza for the whole household", 500, "pizza"),
    Reward("2", "Movie Night", "Rent any movie and some popcorn", 300, "movie"),
    Reward("3", "Game Pass", "One week of gaming without chores", 1000, "game"),
    Reward("4", "House Star", "Choose the next household activity", 200, "star")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    userPoints: Long,
    onBack: () -> Unit,
    onPurchase: (Reward) -> Unit
) {
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

            Text(
                "Available Rewards",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = KippoColors.DarkText,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sampleRewards) { reward ->
                    RewardItem(
                        reward = reward, 
                        canAfford = userPoints >= reward.cost,
                        onPurchase = { onPurchase(reward) }
                    )
                }
            }
        }
    }
}

@Composable
fun RewardItem(reward: Reward, canAfford: Boolean, onPurchase: () -> Unit) {
    val icon = when(reward.icon) {
        "pizza" -> Icons.Default.LocalPizza
        "movie" -> Icons.Default.Movie
        "game" -> Icons.Default.VideogameAsset
        else -> Icons.Default.Star
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
                color = KippoColors.DarkText
            )
            
            Text(
                reward.description,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 14.sp,
                modifier = Modifier.height(30.dp)
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
