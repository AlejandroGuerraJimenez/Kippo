package es.ulpgc.kippo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import es.ulpgc.kippo.ui.components.BottomNavDestination
import es.ulpgc.kippo.ui.components.KippoBottomBar
import es.ulpgc.kippo.ui.components.KippoScaffold
import es.ulpgc.kippo.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

private data class RecurrenceOption(val key: String, val label: String, val shortLabel: String)
private val recurrenceOptions = listOf(
    RecurrenceOption("none",      "No repeat", ""),
    RecurrenceOption("daily",     "Daily",     "Daily"),
    RecurrenceOption("weekly",    "Weekly",    "Weekly"),
    RecurrenceOption("biweekly",  "Biweekly",  "Biweekly"),
    RecurrenceOption("monthly",   "Monthly",   "Monthly")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    householdId: String,
    members: List<User>,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateToExpenses: () -> Unit = {},
    onCreateTaskClick: () -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToComplete by remember { mutableStateOf<Task?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

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
            onUpdate = { title, desc, pts, assignedTo, recurrence, dueDate ->
                viewModel.updateTask(taskToEdit!!.id, title, desc, pts, assignedTo, recurrence, dueDate)
            }
        )
    }

    if (taskToComplete != null) {
        CompleteTaskDialog(
            task = taskToComplete!!,
            onDismiss = { taskToComplete = null },
            onConfirm = { timeSpent ->
                viewModel.toggleTask(taskToComplete!!.id, true, timeSpent)
                taskToComplete = null
            }
        )
    }

    KippoScaffold(
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
        bottomBar = {
            KippoBottomBar(
                selectedDestination = BottomNavDestination.TASKS,
                onHomeClick = onNavigateHome,
                onTasksClick = {},
                onCreateClick = onCreateTaskClick,
                onGastosClick = onNavigateToExpenses,
                onProfileClick = onNavigateProfile
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = KippoColors.Background,
                contentColor = KippoColors.Teal
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "All",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) KippoColors.Teal else KippoColors.DarkText.copy(alpha = 0.5f)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "This Week",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) KippoColors.Teal else KippoColors.DarkText.copy(alpha = 0.5f)
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tasks yet. Create one!", color = KippoColors.DarkText.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(tasks) { task ->
                            TaskItem(
                                task = task,
                                members = members,
                                onToggle = {
                                    if (task.completed) {
                                        viewModel.toggleTask(task.id, false)
                                    } else {
                                        taskToComplete = task
                                    }
                                },
                                onClick = { selectedTask = task }
                            )
                        }
                    }
                }
            } else {
                WeeklyTaskView(
                    tasks = tasks,
                    members = members,
                    onToggle = { task ->
                        if (task.completed) {
                            viewModel.toggleTask(task.id, false)
                        } else {
                            taskToComplete = task
                        }
                    },
                    onTaskClick = { selectedTask = it }
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, members: List<User>, onToggle: () -> Unit, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.ENGLISH) }
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
                
                if (task.dueDate != null && !task.completed) {
                    val isOverdue = task.dueDate.before(Date())
                    Text(
                        text = "Due: ${dateFormat.format(task.dueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) Color(0xFFE53935) else KippoColors.DarkText.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                } else if (task.completed && task.completedAt != null) {
                    val timeStr = task.timeSpentMinutes?.let { minutes ->
                        val h = minutes / 60
                        val m = minutes % 60
                        if (h > 0) " (${h}h ${m}m)" else " (${m}m)"
                    } ?: ""
                    Text(
                        text = "Completed: ${dateFormat.format(task.completedAt)}$timeStr",
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
            
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                val recOpt = recurrenceOptions.find { it.key == task.recurrence }
                if (recOpt != null && recOpt.key != "none") {
                    Surface(
                        color = KippoColors.Teal.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = null,
                                tint = KippoColors.Teal,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = recOpt.shortLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = KippoColors.Teal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTaskView(
    tasks: List<Task>,
    members: List<User>,
    onToggle: (Task) -> Unit,
    onTaskClick: (Task) -> Unit
) {
    var weekOffset by remember { mutableIntStateOf(0) }
    val today = remember { LocalDate.now() }
    val monday = remember(weekOffset) {
        today.with(DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())
    }
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }

    fun Date.toLocalDate(): LocalDate =
        toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    val tasksByDay = remember(tasks, weekDays) {
        weekDays.associateWith { day ->
            tasks.filter { it.dueDate?.toLocalDate() == day }
        }
    }
    val recurringNoDueDate = remember(tasks) {
        tasks.filter { it.recurrence != "none" && it.dueDate == null && !it.completed }
    }

    val dayMonthFormat = remember { SimpleDateFormat("dd MMM", Locale.ENGLISH) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { weekOffset-- }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = KippoColors.Teal)
                }
                Text(
                    text = if (weekOffset == 0) "This Week"
                           else "Week of ${dayMonthFormat.format(Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant()))}",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(onClick = { weekOffset++ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = KippoColors.Teal)
                }
            }
        }

        weekDays.forEach { day ->
            val dayTasks = tasksByDay[day] ?: emptyList()
            val isToday = day == today
            val dayName = day.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                .replaceFirstChar { it.uppercase() }

            item(key = day.toString()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isToday) KippoColors.Teal else KippoColors.Teal.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color.White else KippoColors.Teal,
                            fontSize = 14.sp
                        )
                    }
                    Column {
                        Text(
                            dayName,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isToday) KippoColors.Teal else KippoColors.DarkText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            dayMonthFormat.format(Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                            style = MaterialTheme.typography.labelSmall,
                            color = KippoColors.DarkText.copy(alpha = 0.4f)
                        )
                    }
                    if (dayTasks.isNotEmpty()) {
                        Spacer(Modifier.weight(1f))
                        Surface(
                            color = KippoColors.Yellow.copy(alpha = 0.3f),
                            shape = CircleShape
                        ) {
                            Text(
                                "${dayTasks.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KippoColors.DarkText
                            )
                        }
                    }
                }
            }

            if (dayTasks.isEmpty()) {
                item(key = "${day}_empty") {
                    Text(
                        "No tasks",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 46.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = KippoColors.DarkText.copy(alpha = 0.3f)
                    )
                }
            } else {
                items(dayTasks, key = { it.id }) { task ->
                    Box(modifier = Modifier.padding(start = 46.dp)) {
                        TaskItem(
                            task = task,
                            members = members,
                            onToggle = { onToggle(task) },
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }

        if (recurringNoDueDate.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Repeat, contentDescription = null, tint = KippoColors.Teal, modifier = Modifier.size(18.dp))
                    Text(
                        "Recurring without date",
                        fontWeight = FontWeight.Bold,
                        color = KippoColors.DarkText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(recurringNoDueDate, key = { "rec_${it.id}" }) { task ->
                TaskItem(
                    task = task,
                    members = members,
                    onToggle = { onToggle(task) },
                    onClick = { onTaskClick(task) }
                )
            }
        }
    }
}

@Composable
fun TaskDetailDialog(task: Task, members: List<User>, onDismiss: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH) }
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
                
                DetailRow(label = "Status:", value = if (task.completed) "Completed ✓" else "Pending")
                DetailRow(label = "Points:", value = "${task.points} PTS")
                DetailRow(label = "Assigned to:", value = assignedUser?.username ?: "Anyone")
                DetailRow(
                    label = "Recurrence:",
                    value = recurrenceOptions.find { it.key == task.recurrence }?.label ?: "No repeat"
                )
                DetailRow(label = "Created on:", value = task.createdAt?.let { dateFormat.format(it) } ?: "N/A")
                
                if (task.completed) {
                    DetailRow(label = "Completed by:", value = completedByUser?.username ?: "Unknown")
                    DetailRow(label = "Completed on:", value = task.completedAt?.let { dateFormat.format(it) } ?: "N/A")
                    task.timeSpentMinutes?.let { minutes ->
                        val h = minutes / 60
                        val m = minutes % 60
                        val timeStr = if (h > 0) "${h}h ${m}m" else "$m minutes"
                        DetailRow(label = "Time spent:", value = timeStr)
                    }
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
fun CompleteTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(15) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("How much time did you spend on '${task.title}'?")
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hours Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (hours < 23) hours++ }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = KippoColors.Teal)
                        }
                        Surface(
                            color = KippoColors.Teal.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = hours.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KippoColors.Teal
                                )
                            }
                        }
                        IconButton(onClick = { if (hours > 0) hours-- }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = KippoColors.Teal)
                        }
                        Text("hours", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    
                    Text(
                        ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 24.dp),
                        color = KippoColors.Teal
                    )
                    
                    // Minutes Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (minutes < 59) minutes++ else minutes = 0 }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = KippoColors.Teal)
                        }
                        Surface(
                            color = KippoColors.Teal.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = minutes.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KippoColors.Teal
                                )
                            }
                        }
                        IconButton(onClick = { if (minutes > 0) minutes-- else minutes = 59 }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = KippoColors.Teal)
                        }
                        Text("min", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Total: ${if (hours > 0) "${hours}h " else ""}${minutes}min",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = KippoColors.DarkText
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalMinutes = (hours * 60) + minutes
                    onConfirm(totalMinutes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("MARK AS DONE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    members: List<User>,
    onDismiss: () -> Unit,
    onCreate: (String, String, Long, String?, String, Date?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("10") }
    var assignedTo by remember { mutableStateOf<String?>(null) }
    var selectedRecurrence by remember { mutableStateOf("none") }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val dateDisplayFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = KippoColors.Teal) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = KippoColors.DarkTeal)
                }
            }
        ) { DatePicker(state = datePickerState) }
    }
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
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(vertical = 24.dp),
        title = {
            Text(
                "Add New Task",
                fontWeight = FontWeight.Bold,
                color = KippoColors.DarkText
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("Ex: Take out the trash") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = points,
                    onValueChange = { if (it.all { char -> char.isDigit() }) points = it },
                    label = { Text("Points reward") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                
                Column {
                    Text(
                        text = "Assign to",
                        style = MaterialTheme.typography.labelMedium,
                        color = KippoColors.DarkText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KippoColors.DarkText),
                            border = BorderStroke(1.dp, KippoColors.Yellow)
                        ) {
                            val selectedName = members.find { it.uid == assignedTo }?.username ?: "Anyone"
                            Text(selectedName, color = if (assignedTo == null) KippoColors.DarkText.copy(alpha = 0.6f) else KippoColors.Teal)
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

                Column {
                    Text(
                        text = "Due date",
                        style = MaterialTheme.typography.labelMedium,
                        color = KippoColors.DarkText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KippoColors.DarkText),
                        border = BorderStroke(1.dp, KippoColors.DarkTeal.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = KippoColors.Teal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = dueDateMillis?.let { dateDisplayFormat.format(Date(it)) } ?: "No date",
                            modifier = Modifier.weight(1f),
                            color = if (dueDateMillis != null) KippoColors.Teal else KippoColors.DarkText.copy(alpha = 0.5f)
                        )
                        if (dueDateMillis != null) {
                            IconButton(onClick = { dueDateMillis = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "Repeat",
                        style = MaterialTheme.typography.labelMedium,
                        color = KippoColors.DarkText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recurrenceOptions) { opt ->
                            FilterChip(
                                selected = selectedRecurrence == opt.key,
                                onClick = { selectedRecurrence = opt.key },
                                label = { Text(opt.label, fontSize = 12.sp) },
                                leadingIcon = if (opt.key != "none") {
                                    {
                                        Icon(
                                            Icons.Default.Repeat,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KippoColors.Teal,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val dueDate = dueDateMillis?.let { Date(it) }
                        onCreate(title, description, points.toLongOrNull() ?: 0L, assignedTo, selectedRecurrence, dueDate)
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = KippoColors.DarkTeal)
            ) {
                Text("CANCEL")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    members: List<User>,
    onDismiss: () -> Unit,
    onUpdate: (String, String, Long, String?, String, Date?) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var points by remember { mutableStateOf(task.points.toString()) }
    var assignedTo by remember { mutableStateOf(task.assignedTo) }
    var selectedRecurrence by remember { mutableStateOf(task.recurrence) }
    var dueDateMillis by remember { mutableStateOf<Long?>(task.dueDate?.time) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val dateDisplayFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = KippoColors.Teal) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = KippoColors.DarkTeal)
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

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

                Column {
                    Text(
                        "Due date",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = KippoColors.Teal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = dueDateMillis?.let { dateDisplayFormat.format(Date(it)) } ?: "No date",
                            modifier = Modifier.weight(1f),
                            color = if (dueDateMillis != null) KippoColors.Teal else Color.Gray
                        )
                        if (dueDateMillis != null) {
                            IconButton(onClick = { dueDateMillis = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Column {
                    Text(
                        "Repeat",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recurrenceOptions) { opt ->
                            FilterChip(
                                selected = selectedRecurrence == opt.key,
                                onClick = { selectedRecurrence = opt.key },
                                label = { Text(opt.label, fontSize = 12.sp) },
                                leadingIcon = if (opt.key != "none") {
                                    {
                                        Icon(
                                            Icons.Default.Repeat,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KippoColors.Teal,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val dueDate = dueDateMillis?.let { Date(it) }
                        onUpdate(title, description, points.toLongOrNull() ?: 0L, assignedTo, selectedRecurrence, dueDate)
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
