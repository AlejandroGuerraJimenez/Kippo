package es.ulpgc.kippo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.ulpgc.kippo.viewmodel.GroceryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroceryListScreen(
    householdId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onListCreated: () -> Unit,
    viewModel: GroceryViewModel
) {
    var listName by remember { mutableStateOf("") }
    var itemText by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Lista de Compra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KippoColors.Background,
                    titleContentColor = KippoColors.DarkText
                )
            )
        },
        containerColor = KippoColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                label = { Text("Nombre de la lista (ej. Mercadona)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KippoColors.Teal,
                    cursorColor = KippoColors.Teal,
                    focusedLabelColor = KippoColors.Teal
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                "Añadir productos",
                fontWeight = FontWeight.Bold,
                color = KippoColors.DarkText,
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = itemText,
                    onValueChange = { itemText = it },
                    placeholder = { Text("Ej. Leche") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (itemText.isNotBlank()) {
                            items.add(itemText)
                            itemText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(items) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item, color = KippoColors.DarkText)
                            IconButton(onClick = { items.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (listName.isNotBlank()) {
                        viewModel.createGroceryList(listName, householdId, currentUserId, items)
                        onListCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(16.dp),
                enabled = listName.isNotBlank()
            ) {
                Text("Crear Lista", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
