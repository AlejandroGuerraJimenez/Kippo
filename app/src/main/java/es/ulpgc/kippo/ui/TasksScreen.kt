package es.ulpgc.kippo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.model.Task
import es.ulpgc.kippo.model.User
import es.ulpgc.kippo.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    householdId: String,
    members: List<User>,
    onBack: () -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(householdId) {
        viewModel.observeTasks(householdId)
    }

    if (selectedTask != null) {
        TaskDetailDialog(
            task = selectedTask!!,
            members = members,
            onDismiss = { selectedTask = null },
            onEdit = {
                taskToEdit = selectedTask
                selectedTask = null
            },
            onDelete = {
                viewModel.deleteTask(selectedTask!!.id)
                selectedTask = null
            }
        )
    }

    if (taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            members = members,
            onDismiss = { taskToEdit = null },
            onUpdate = { title, desc, pts, assignedTo ->
                viewModel.updateTask(taskToEdit!!.id, title, desc, pts, assignedTo)
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Household Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KippoColors.Background
                )
            )
        },
        containerColor = KippoColors.Background
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tasks yet. Create one!", color = KippoColors.DarkText.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(tasks) { task ->
                    TaskItem(
                        task = task, 
                        members = members,
                        onToggle = { viewModel.toggleTask(task.id, !task.completed) },
                        onClick = { selectedTask = task }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, members: List<User>, onToggle: () -> Unit, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val assignedUser = members.find { it.uid == task.assignedTo }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.completed) KippoColors.Teal else Color.LightGray,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                    color = if (task.completed) KippoColors.DarkText.copy(alpha = 0.5f) else KippoColors.DarkText
                )
                
                if (task.completed && task.completedAt != null) {
                    Text(
                        text = "Completed: ${dateFormat.format(task.completedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = KippoColors.Teal,
                        fontWeight = FontWeight.Medium
                    )
                } else if (assignedUser != null) {
                    Text(
                        text = "Assigned to: ${assignedUser.username}",
                        style = MaterialTheme.typography.labelSmall,
                        color = KippoColors.Teal,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Unassigned",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            
            Surface(
                color = if (task.completed) Color.LightGray.copy(alpha = 0.2f) else KippoColors.Yellow.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${task.points} PTS",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (task.completed) Color.Gray else KippoColors.DarkText
                )
            }
        }
    }
}

@Composable
fun TaskDetailDialog(task: Task, members: List<User>, onDismiss: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val assignedUser = members.find { it.uid == task.assignedTo }
    val completedByUser = members.find { it.uid == task.completedBy }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(task.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (task.description.isNotBlank()) {
                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                DetailRow(label = "Status:", value = if (task.completed) "Completed " else "Pending ")
                DetailRow(label = "Points:", value = "${task.points} PTS")
                DetailRow(label = "Assigned to:", value = assignedUser?.username ?: "Anyone")
                DetailRow(label = "Created on:", value = task.createdAt?.let { dateFormat.format(it) } ?: "N/A")
                
                if (task.completed) {
                    DetailRow(label = "Completed by:", value = completedByUser?.username ?: "Unknown")
                    DetailRow(label = "Completed on:", value = task.completedAt?.let { dateFormat.format(it) } ?: "N/A")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE") }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
    }
}

@Composable
fun CreateTaskDialog(
    members: List<User>,
    onDismiss: () -> Unit,
    onCreate: (String, String, Long, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("10") }
    var assignedTo by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(vertical = 24.dp),
        title = { Text("Add New Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("Ej: Sacar la basura") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = points,
                    onValueChange = { if (it.all { char -> char.isDigit() }) points = it },
                    label = { Text("Points reward") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Column {
                    Text(
                        text = "Assign to",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val selectedName = members.find { it.uid == assignedTo }?.username ?: "Anyone"
                            Text(selectedName, color = if (assignedTo == null) Color.Gray else KippoColors.Teal)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expanded, 
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Anyone (Public)") },
                                onClick = { assignedTo = null; expanded = false }
                            )
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.username) },
                                    onClick = { assignedTo = member.uid; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, description, points.toLongOrNull() ?: 0L, assignedTo)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ADD TASK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}

@Composable
fun EditTaskDialog(
    task: Task,
    members: List<User>,
    onDismiss: () -> Unit,
    onUpdate: (String, String, Long, String?) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var points by remember { mutableStateOf(task.points.toString()) }
    var assignedTo by remember { mutableStateOf(task.assignedTo) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.92f),
        title = { Text("Edit Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = points,
                    onValueChange = { if (it.all { char -> char.isDigit() }) points = it },
                    label = { Text("Points reward") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Column {
                    Text("Assign to", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val selectedName = members.find { it.uid == assignedTo }?.username ?: "Anyone"
                            Text(selectedName, color = if (assignedTo == null) Color.Gray else KippoColors.Teal)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Anyone (Public)") },
                                onClick = { assignedTo = null; expanded = false }
                            )
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.username) },
                                    onClick = { assignedTo = member.uid; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onUpdate(title, description, points.toLongOrNull() ?: 0L, assignedTo)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}
