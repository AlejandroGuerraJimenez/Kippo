package es.ulpgc.kippo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.ui.components.KippoScaffold
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

    KippoScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Join Household") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KippoColors.Background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
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
                        text = "Join your household",
                        style = MaterialTheme.typography.headlineSmall,
                        color = KippoColors.DarkText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Introduce el código de 6 dígitos que te compartieron",
                        style = MaterialTheme.typography.bodyLarge,
                        color = KippoColors.DarkText.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = viewModel::onJoinCodeChange,
                        label = { Text("Código") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = joinState !is JoinHouseholdViewModel.JoinState.Loading,
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

                    Spacer(modifier = Modifier.height(20.dp))

                    if (joinState is JoinHouseholdViewModel.JoinState.Loading) {
                        CircularProgressIndicator(color = KippoColors.Teal)
                    } else {
                        Button(
                            onClick = viewModel::joinHousehold,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("JOIN HOUSEHOLD")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = joinState !is JoinHouseholdViewModel.JoinState.Loading,
                        border = androidx.compose.foundation.BorderStroke(1.dp, KippoColors.Yellow),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KippoColors.DarkText),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("BACK")
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
    }
}
