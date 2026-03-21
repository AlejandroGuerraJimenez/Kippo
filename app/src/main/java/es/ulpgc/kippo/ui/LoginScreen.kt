package es.ulpgc.kippo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.R
import es.ulpgc.kippo.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.onLoginScreenShown()
    }

    val email = viewModel.email.value
    val password = viewModel.password.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value
    val isSuccess = viewModel.isSuccess.value

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onLoginSuccess()
            viewModel.consumeLoginSuccess()
        }
    }

    val kippoTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KippoColors.Teal,
        unfocusedBorderColor = KippoColors.DarkTeal.copy(alpha = 0.3f),
        focusedLabelColor = KippoColors.Teal,
        unfocusedLabelColor = KippoColors.DarkText.copy(alpha = 0.6f),
        cursorColor = KippoColors.Teal,
        focusedTextColor = KippoColors.DarkText,
        unfocusedTextColor = KippoColors.DarkText
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KippoColors.Background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo and App Name OUTSIDE the Card
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(100.dp),
            shadowElevation = 4.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_kippo),
                contentDescription = "Logo",
                modifier = Modifier
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Kippo",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KippoColors.Teal
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = kippoTextFieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = kippoTextFieldColors,
                    singleLine = true
                )

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }


                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.onLoginClick() },
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
                        Text("Iniciar sesión", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onNavigateToRegister,
                    colors = ButtonDefaults.textButtonColors(contentColor = KippoColors.Teal)
                ) {
                    Text("¿No tienes cuenta? Regístrate", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}