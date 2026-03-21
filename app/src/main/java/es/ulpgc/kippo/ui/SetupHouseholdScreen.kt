package es.ulpgc.kippo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupHouseholdScreen(
    onCreateHouseholdClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Welcome to Kippo", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = KippoColors.Teal
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "You don't have a household yet",
                style = MaterialTheme.typography.headlineSmall,
                color = KippoColors.DarkText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "To start using Kippo, you need to create a new household or join an existing one.",
                style = MaterialTheme.typography.bodyLarge,
                color = KippoColors.DarkText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onCreateHouseholdClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AddHome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("CREATE HOUSEHOLD", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onSignOut) {
                Text("Sign out", color = KippoColors.DarkTeal)
            }
        }
    }
}
