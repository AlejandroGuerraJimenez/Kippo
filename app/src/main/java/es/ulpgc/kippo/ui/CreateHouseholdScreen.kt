package es.ulpgc.kippo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.ui.KippoColors
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
            TopAppBar(title = { Text("Create New Household") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = householdName,
                onValueChange = { viewModel.onHouseholdNameChange(it) },
                label = { Text("Household Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = creationState !is CreateHouseholdViewModel.CreationState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (creationState is CreateHouseholdViewModel.CreationState.Loading) {
                CircularProgressIndicator(color = KippoColors.Teal)
            } else {
                Button(
                    onClick = { viewModel.createHousehold() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal)
                ) {
                    Text("Create Household")
                }
            }

            if (creationState is CreateHouseholdViewModel.CreationState.Error) {
                val errorMessage = (creationState as CreateHouseholdViewModel.CreationState.Error).message
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
