package es.ulpgc.kippo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.viewmodel.JoinHouseholdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinHouseholdScreen(
    onHouseholdJoined: () -> Unit,
    onBack: () -> Unit,
    viewModel: JoinHouseholdViewModel = viewModel()
) {
    val joinCode by viewModel.joinCode.collectAsState()
    val joinState by viewModel.joinState.collectAsState()

    LaunchedEffect(joinState) {
        if (joinState is JoinHouseholdViewModel.JoinState.Success) {
            onHouseholdJoined()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Household") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Introduce el código de 6 dígitos que te compartieron",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = joinCode,
                onValueChange = viewModel::onJoinCodeChange,
                label = { Text("Código") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = joinState !is JoinHouseholdViewModel.JoinState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (joinState is JoinHouseholdViewModel.JoinState.Loading) {
                CircularProgressIndicator(color = KippoColors.Teal)
            } else {
                Button(
                    onClick = viewModel::joinHousehold,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal)
                ) {
                    Text("Join Household")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = joinState !is JoinHouseholdViewModel.JoinState.Loading
            ) {
                Text("Back")
            }

            if (joinState is JoinHouseholdViewModel.JoinState.Error) {
                val message = (joinState as JoinHouseholdViewModel.JoinState.Error).message
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
