package es.ulpgc.kippo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.ulpgc.kippo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSignOut: () -> Unit = {}) {
    Scaffold(
        topBar = { KippoTopBar() },
        bottomBar = { KippoBottomBar() },
        containerColor = KippoColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButtonsRow()
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Placeholder for cards since no data yet
            Text(
                text = "Welcome to your Home",
                color = KippoColors.DarkText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.DarkTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar sesión")
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun KippoTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile/Logo Circle
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
                text = "Home",
                fontSize = 15.sp,
                color = KippoColors.DarkText.copy(alpha = 0.6f)
            )
        }
        
        // Notification Bell with Dot
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = KippoColors.DarkText,
                modifier = Modifier.size(28.dp)
            )
            Surface(
                color = Color(0xFFE57373), // Soft red dot
                shape = CircleShape,
                modifier = Modifier
                    .size(10.dp)
                    .offset(x = (-2).dp, y = (2).dp),
                border = BorderStroke(1.5.dp, KippoColors.Background)
            ) {}
        }
    }
}

@Composable
fun ActionButtonsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ACTIONS Button
        Button(
            onClick = { },
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
            shape = RoundedCornerShape(27.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "ACTIONS",
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }

        // REWARDS Button
        Button(
            onClick = { },
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Yellow),
            shape = RoundedCornerShape(27.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                tint = KippoColors.DarkText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "REWARDS",
                color = KippoColors.DarkText,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun KippoBottomBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Home, 
                        contentDescription = "Home", 
                        tint = KippoColors.Teal,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.BusinessCenter, 
                        contentDescription = "Tasks", 
                        tint = Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(60.dp))

                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.CalendarMonth, 
                        contentDescription = "Calendar", 
                        tint = Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Outlined.Person, 
                        contentDescription = "Profile", 
                        tint = Color.LightGray,
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
                .offset(y = (-12).dp),
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
                            .padding(0.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}
