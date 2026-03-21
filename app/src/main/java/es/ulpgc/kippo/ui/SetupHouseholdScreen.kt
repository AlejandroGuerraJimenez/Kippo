package es.ulpgc.kippo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupHouseholdScreen(
    onCreateHouseholdClick: () -> Unit,
    onJoinHouseholdClick: () -> Unit,
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                        text = "To start using Kippo, create a household or join with an invite code.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = KippoColors.DarkText.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onJoinHouseholdClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KippoColors.Yellow),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KippoColors.DarkText)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("JOIN WITH CODE", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.textButtonColors(contentColor = KippoColors.DarkTeal)
                    ) {
                        Text("Sign out")
                    }
                }
            }
        }
    }
}