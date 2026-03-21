package es.ulpgc.kippo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileSection(
    name: String,
    username: String,
    email: String,
    points: Long,
    profileIconKey: String,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit
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
            Surface(
                shape = CircleShape,
                color = KippoColors.Background,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val avatarTint = if (profileIconKey == "placeholder_avatar") KippoColors.DarkTeal else KippoColors.Teal
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile avatar",
                        tint = avatarTint,
                        modifier = Modifier.size(84.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name.ifBlank { "Sin nombre" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KippoColors.DarkText
            )
            Text(
                text = "@$username",
                style = MaterialTheme.typography.bodyMedium,
                color = KippoColors.DarkText.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = KippoColors.DarkText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Puntos acumulados: $points",
                style = MaterialTheme.typography.bodyLarge,
                color = KippoColors.DarkTeal,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KippoColors.DarkText),
                border = BorderStroke(1.dp, KippoColors.Yellow),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Editar perfil")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.DarkTeal),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    initialName: String,
    initialUsername: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KippoColors.Teal,
        unfocusedBorderColor = KippoColors.DarkTeal.copy(alpha = 0.3f),
        focusedLabelColor = KippoColors.Teal,
        unfocusedLabelColor = KippoColors.DarkText.copy(alpha = 0.6f),
        cursorColor = KippoColors.Teal,
        focusedTextColor = KippoColors.DarkText,
        unfocusedTextColor = KippoColors.DarkText
    )

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    enabled = !isSaving,
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    enabled = !isSaving,
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, username) },
                enabled = !isSaving,
                colors = ButtonDefaults.textButtonColors(contentColor = KippoColors.Yellow)
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving,
                colors = ButtonDefaults.textButtonColors(contentColor = KippoColors.DarkTeal)
            ) {
                Text("Cancelar")
            }
        }
    )
}


