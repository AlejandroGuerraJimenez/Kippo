package es.ulpgc.kippo.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.ulpgc.kippo.model.GroceryItem
import es.ulpgc.kippo.model.GroceryList
import es.ulpgc.kippo.viewmodel.GroceryViewModel
import es.ulpgc.kippo.ui.components.BottomNavDestination
import es.ulpgc.kippo.ui.components.KippoBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    householdId: String,
    currentUserId: String,
    onNavigateHome: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToGastos: () -> Unit,
    onAddGroceryClick: () -> Unit,
    viewModel: GroceryViewModel
) {
    val groceryLists by viewModel.groceryLists.collectAsState()
    val itemsMap by viewModel.items.collectAsState()

    LaunchedEffect(householdId) {
        viewModel.observeGroceryLists(householdId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Listas de Compra", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KippoColors.Background
                )
            )
        },
        bottomBar = {
            KippoBottomBar(
                selectedDestination = BottomNavDestination.HOME,
                onHomeClick = onNavigateHome,
                onTasksClick = onNavigateToTasks,
                onCreateClick = onAddGroceryClick,
                onGastosClick = onNavigateToGastos,
                onProfileClick = { /* Navigate to profile */ }
            )
        },
        containerColor = KippoColors.Background
    ) { padding ->
        if (groceryLists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay listas de compra todavía", color = KippoColors.DarkText.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(groceryLists) { list ->
                    GroceryListCard(
                        list = list,
                        items = itemsMap[list.id] ?: emptyList(),
                        onToggleItem = { itemId, checked ->
                            viewModel.toggleItemChecked(list.id, itemId, checked)
                        },
                        onAddItem = { itemName ->
                            viewModel.addItemToList(list.id, itemName, currentUserId)
                        },
                        onDeleteList = { viewModel.deleteList(list.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun GroceryListCard(
    list: GroceryList,
    items: List<GroceryItem>,
    onToggleItem: (String, Boolean) -> Unit,
    onAddItem: (String) -> Unit,
    onDeleteList: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var newItemName by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteList) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.4f))
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expandir"
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleItem(item.id, !item.checked) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { onToggleItem(item.id, it) },
                            colors = CheckboxDefaults.colors(checkedColor = KippoColors.Teal)
                        )
                        Text(
                            text = item.name,
                            textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                            color = if (item.checked) KippoColors.DarkText.copy(alpha = 0.5f) else KippoColors.DarkText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        placeholder = { Text("Añadir artículo...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = KippoColors.Teal
                        )
                    )
                    IconButton(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                onAddItem(newItemName)
                                newItemName = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Añadir", tint = KippoColors.Teal)
                    }
                }
            }
        }
    }
}
