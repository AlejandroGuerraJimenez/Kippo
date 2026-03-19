package es.ulpgc.kippo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.viewmodel.RegisterViewModel

// colors moved to Colors.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val email = viewModel.email.value
    val password = viewModel.password.value
    val username = viewModel.username.value
    val name = viewModel.name.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value
    val isSuccess = viewModel.isSuccess.value
    val fieldErrors = viewModel.fieldErrors.value

    val kippoTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KippoColors.Teal,
        unfocusedBorderColor = KippoColors.DarkTeal.copy(alpha = 0.3f),
        focusedLabelColor = KippoColors.Teal,
        unfocusedLabelColor = KippoColors.DarkText.copy(alpha = 0.6f),
        cursorColor = KippoColors.Teal,
        focusedTextColor = KippoColors.DarkText,
        unfocusedTextColor = KippoColors.DarkText
    )

    Box(
        modifier = Modifier
                .fillMaxSize()
                .background(KippoColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Registro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = fieldErrors["username"] != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = kippoTextFieldColors,
                    singleLine = true
                )
                if (fieldErrors["username"] != null) {
                    Text(
                        text = fieldErrors["username"] ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = fieldErrors["name"] != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = kippoTextFieldColors,
                    singleLine = true
                )
                if (fieldErrors["name"] != null) {
                    Text(
                        text = fieldErrors["name"] ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = fieldErrors["email"] != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = kippoTextFieldColors,
                    singleLine = true
                )
                if (fieldErrors["email"] != null) {
                    Text(
                        text = fieldErrors["email"] ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading,
                    isError = fieldErrors["password"] != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = kippoTextFieldColors,
                    singleLine = true
                )
                if (fieldErrors["password"] != null) {
                    Text(
                        text = fieldErrors["password"] ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.textButtonColors(contentColor = KippoColors.Teal)
                ) {
                    Text("¿Ya tienes cuenta? Inicia sesión", fontWeight = FontWeight.SemiBold)
                }

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (isSuccess) {
                    Text(
                        text = "¡Registro completado con éxito!",
                            color = KippoColors.Teal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    onRegisterSuccess()
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.onRegisterClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KippoColors.Teal,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Registrarse", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}