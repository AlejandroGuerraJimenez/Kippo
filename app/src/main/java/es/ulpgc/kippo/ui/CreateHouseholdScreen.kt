package es.ulpgc.kippo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.viewmodel.CreateHouseholdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHouseholdScreen(
    onHouseholdCreated: () -> Unit,
    viewModel: CreateHouseholdViewModel = viewModel()
) {
    val householdName by viewModel.householdName.collectAsState()
    val creationState by viewModel.creationState.collectAsState()

    LaunchedEffect(creationState) {
        if (creationState is CreateHouseholdViewModel.CreationState.Success) {
            onHouseholdCreated()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Household", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KippoColors.Background
                )
            )
        },
        containerColor = KippoColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create a new household",
                        style = MaterialTheme.typography.headlineSmall,
                        color = KippoColors.DarkText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We will generate a 6-digit code so others can join.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = KippoColors.DarkText.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = householdName,
                        onValueChange = { viewModel.onHouseholdNameChange(it) },
                        label = { Text("Household name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = creationState !is CreateHouseholdViewModel.CreationState.Loading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KippoColors.Teal,
                            unfocusedBorderColor = KippoColors.DarkTeal.copy(alpha = 0.3f),
                            focusedLabelColor = KippoColors.Teal,
                            unfocusedLabelColor = KippoColors.DarkText.copy(alpha = 0.6f),
                            cursorColor = KippoColors.Teal,
                            focusedTextColor = KippoColors.DarkText,
                            unfocusedTextColor = KippoColors.DarkText
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (creationState is CreateHouseholdViewModel.CreationState.Loading) {
                        CircularProgressIndicator(color = KippoColors.Teal)
                    } else {
                        Button(
                            onClick = { viewModel.createHousehold() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("CREATE HOUSEHOLD")
                        }
                    }

                    if (creationState is CreateHouseholdViewModel.CreationState.Error) {
                        val errorMessage = (creationState as CreateHouseholdViewModel.CreationState.Error).message
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
