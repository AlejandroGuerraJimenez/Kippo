package es.ulpgc.kippo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import es.ulpgc.kippo.R
import es.ulpgc.kippo.ui.KippoColors

enum class BottomNavDestination {
    HOME,
    TASKS,
    GASTOS,
    PROFILE
}

@Composable
fun KippoBottomBar(
    selectedDestination: BottomNavDestination,
    onHomeClick: () -> Unit,
    onTasksClick: () -> Unit,
    onCreateClick: () -> Unit,
    onGastosClick: () -> Unit = {},
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(90.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onHomeClick) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (selectedDestination == BottomNavDestination.HOME) KippoColors.Teal else Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onTasksClick) {
                    Icon(
                        Icons.Default.TaskAlt,
                        contentDescription = "Tasks",
                        tint = if (selectedDestination == BottomNavDestination.TASKS) KippoColors.Teal else Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(60.dp))

                IconButton(onClick = onGastosClick) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = "Expenses",
                        tint = if (selectedDestination == BottomNavDestination.GASTOS) KippoColors.Teal else Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = if (selectedDestination == BottomNavDestination.PROFILE) KippoColors.Teal else Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = KippoColors.Yellow,
            modifier = Modifier
                .size(74.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .clickable { onCreateClick() },
            shadowElevation = 6.dp,
            border = BorderStroke(4.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(54.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_kippo_transparent),
                        contentDescription = "Main Logo",
                        modifier = Modifier
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}
