package es.ulpgc.kippo.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.model.Expense
import es.ulpgc.kippo.model.ExpenseCategory
import es.ulpgc.kippo.model.User
import es.ulpgc.kippo.ui.components.PhotoSourceBottomSheet
import es.ulpgc.kippo.ui.components.BottomNavDestination
import es.ulpgc.kippo.ui.components.KippoBottomBar
import es.ulpgc.kippo.util.ImageUtils
import es.ulpgc.kippo.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    householdId: String,
    members: List<User>,
    currentUserId: String,
    onNavigateHome: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onAddExpenseClick: () -> Unit,
    viewModel: ExpenseViewModel = viewModel()
) {
    val expenses by viewModel.expenses.collectAsState()
    val netBalances by viewModel.netBalances.collectAsState()
    val simplifiedDebts by viewModel.simplifiedDebts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var showSettleUpDialog by remember { mutableStateOf(false) }
    var settleUpFromUid by remember { mutableStateOf("") }
    var settleUpToUid by remember { mutableStateOf("") }
    var settleUpAmount by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(householdId) {
        viewModel.observeExpenses(householdId)
    }

    if (selectedExpense != null) {
        ExpenseDetailDialog(
            expense = selectedExpense!!,
            members = members,
            currentUserId = currentUserId,
            onDismiss = { selectedExpense = null },
            onDelete = {
                viewModel.deleteExpense(selectedExpense!!.id)
                selectedExpense = null
            }
        )
    }

    if (showSettleUpDialog) {
        SettleUpDialog(
            fromUid = settleUpFromUid,
            toUid = settleUpToUid,
            prefilledAmount = settleUpAmount,
            members = members,
            onDismiss = { showSettleUpDialog = false },
            onConfirm = { fromUid, toUid, amount, note ->
                viewModel.settleUp(fromUid, toUid, amount, note)
                showSettleUpDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Expenses", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = KippoColors.Background
                )
            )
        },
        bottomBar = {
            KippoBottomBar(
                selectedDestination = BottomNavDestination.GASTOS,
                onHomeClick = onNavigateHome,
                onTasksClick = onNavigateToTasks,
                onCreateClick = onAddExpenseClick,
                onGastosClick = {},
                onProfileClick = onNavigateToProfile
            )
        },
        containerColor = KippoColors.Background
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
                            "History",
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
                            "Balances",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) KippoColors.Teal else KippoColors.DarkText.copy(alpha = 0.5f)
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = KippoColors.Teal.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No expenses yet",
                                color = KippoColors.DarkText.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Tap + to add one",
                                color = KippoColors.DarkText.copy(alpha = 0.3f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(expenses) { expense ->
                            ExpenseItem(
                                expense = expense,
                                members = members,
                                currentUserId = currentUserId,
                                onClick = { selectedExpense = expense }
                            )
                        }
                    }
                }
            } else {
                BalanceSummaryContent(
                    netBalances = netBalances,
                    simplifiedDebts = simplifiedDebts,
                    members = members,
                    currentUserId = currentUserId,
                    onSettleUp = { fromUid, toUid, amount ->
                        settleUpFromUid = fromUid
                        settleUpToUid = toUid
                        settleUpAmount = amount
                        showSettleUpDialog = true
                    }
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    members: List<User>,
    currentUserId: String,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val paidByUser = members.find { it.uid == expense.paidBy }
    val category = ExpenseCategory.fromKey(expense.category)
    val shareAmount = if (expense.splitAmong.isNotEmpty())
        expense.amount / expense.splitAmong.size else expense.amount
    val isInSplit = currentUserId in expense.splitAmong
    val isPayer = currentUserId == expense.paidBy
    val amountColor = when {
        isPayer && isInSplit -> KippoColors.Teal
        isPayer -> KippoColors.Teal
        isInSplit -> Color(0xFFE53935)
        else -> KippoColors.DarkText.copy(alpha = 0.4f)
    }

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
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(KippoColors.Teal.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(category),
                    contentDescription = category.label,
                    tint = KippoColors.Teal,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText
                )
                Text(
                    text = "Paid by: ${paidByUser?.username ?: "Unknown"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = KippoColors.DarkText.copy(alpha = 0.6f)
                )
                expense.createdAt?.let {
                    Text(
                        text = dateFormat.format(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = KippoColors.DarkText.copy(alpha = 0.4f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f €".format(expense.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = amountColor
                )
                Text(
                    text = "${expense.splitAmong.size} people",
                    style = MaterialTheme.typography.labelSmall,
                    color = KippoColors.DarkText.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun BalanceSummaryContent(
    netBalances: Map<String, Double>,
    simplifiedDebts: List<Triple<String, String, Double>>,
    members: List<User>,
    currentUserId: String,
    onSettleUp: (fromUid: String, toUid: String, amount: Double) -> Unit
) {
    if (netBalances.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = KippoColors.Teal.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Everything is settled",
                    color = KippoColors.DarkText.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Balance summary",
                fontWeight = FontWeight.Bold,
                color = KippoColors.DarkText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(members) { member ->
            val balance = netBalances[member.uid] ?: 0.0
            BalanceMemberRow(member = member, balance = balance)
        }

        if (simplifiedDebts.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "To settle up",
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(simplifiedDebts) { (fromUid, toUid, amount) ->
                val fromUser = members.find { it.uid == fromUid }
                val toUser = members.find { it.uid == toUid }
                DebtRow(
                    fromUsername = fromUser?.username ?: "?",
                    toUsername = toUser?.username ?: "?",
                    amount = amount,
                    isCurrentUserDebtor = fromUid == currentUserId,
                    onSettleUp = { onSettleUp(fromUid, toUid, amount) }
                )
            }
        }
    }
}

@Composable
fun BalanceMemberRow(member: User, balance: Double) {
    val isPositive = balance > 0.01
    val isNegative = balance < -0.01
    val balanceColor = when {
        isPositive -> KippoColors.Teal
        isNegative -> Color(0xFFE53935)
        else -> KippoColors.DarkText.copy(alpha = 0.4f)
    }
    val balanceText = when {
        isPositive -> "+%.2f €".format(balance)
        isNegative -> "%.2f €".format(balance)
        else -> "0.00 €"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(KippoColors.Teal.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.username.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.Teal,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = member.username,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                color = KippoColors.DarkText
            )
            Text(
                text = balanceText,
                fontWeight = FontWeight.ExtraBold,
                color = balanceColor,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun DebtRow(
    fromUsername: String,
    toUsername: String,
    amount: Double,
    isCurrentUserDebtor: Boolean,
    onSettleUp: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUserDebtor)
                Color(0xFFFFEBEE) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = fromUsername,
                fontWeight = FontWeight.Bold,
                color = KippoColors.DarkText,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = KippoColors.DarkText.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = toUsername,
                fontWeight = FontWeight.Bold,
                color = KippoColors.DarkText,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "%.2f €".format(amount),
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE53935),
                fontSize = 14.sp
            )
            Button(
                onClick = onSettleUp,
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("SETTLE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExpenseDetailDialog(
    expense: Expense,
    members: List<User>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val paidByUser = members.find { it.uid == expense.paidBy }
    val splitUsers = expense.splitAmong.mapNotNull { uid -> members.find { it.uid == uid }?.username }
    val category = ExpenseCategory.fromKey(expense.category)
    val canDelete = currentUserId == expense.createdBy

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(KippoColors.Teal.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(category),
                        contentDescription = null,
                        tint = KippoColors.Teal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(expense.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider()
                ExpenseDetailRow("Amount:", "%.2f €".format(expense.amount))
                ExpenseDetailRow("Category:", category.label)
                ExpenseDetailRow("Paid by:", paidByUser?.username ?: "Unknown")
                ExpenseDetailRow(
                    "Split between:",
                    if (splitUsers.isEmpty()) "—" else splitUsers.joinToString(", ")
                )
                if (expense.customSplits.isNotEmpty()) {
                    expense.customSplits.forEach { (uid, amount) ->
                        val username = members.find { it.uid == uid }?.username ?: uid
                        ExpenseDetailRow("  $username:", "%.2f €".format(amount))
                    }
                } else {
                    ExpenseDetailRow(
                        "Per person:",
                        if (expense.splitAmong.isNotEmpty())
                            "%.2f €".format(expense.amount / expense.splitAmong.size)
                        else "—"
                    )
                }
                if (expense.notes.isNotBlank()) {
                    ExpenseDetailRow("Notes:", expense.notes)
                }
                val receiptBitmap = remember(expense.receiptImageBase64) {
                    ImageUtils.decodeToImageBitmapOrNull(expense.receiptImageBase64)
                }
                if (receiptBitmap != null) {
                    Text(
                        text = "Receipt:",
                        style = MaterialTheme.typography.bodySmall,
                        color = KippoColors.DarkText.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            bitmap = receiptBitmap,
                            contentDescription = "Receipt image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                expense.createdAt?.let {
                    ExpenseDetailRow("Date:", dateFormat.format(it))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = KippoColors.Teal) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ExpenseDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(110.dp),
            style = MaterialTheme.typography.bodySmall,
            color = KippoColors.DarkText.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = KippoColors.DarkText
        )
    }
}

private enum class SplitMode { EQUAL, CUSTOM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    members: List<User>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        amount: Double,
        paidBy: String,
        splitAmong: List<String>,
        category: String,
        notes: String,
        customSplits: Map<String, Double>,
        receiptImageBase64: String?
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf(currentUserId) }
    var splitAmong by remember { mutableStateOf(members.map { it.uid }.toSet()) }
    var splitMode by remember { mutableStateOf(SplitMode.EQUAL) }
    var customAmounts by remember { mutableStateOf(members.associate { it.uid to "" }) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTRO) }
    var notes by remember { mutableStateOf("") }
    var receiptImageBase64 by remember { mutableStateOf<String?>(null) }
    var showPhotoOptionsSheet by remember { mutableStateOf(false) }
    var paidByExpanded by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var customSplitError by remember { mutableStateOf(false) }

    val totalAmount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val customSum = customAmounts
        .filter { it.key in splitAmong }
        .values.sumOf { it.replace(",", ".").toDoubleOrNull() ?: 0.0 }
    val customSumOk = splitMode == SplitMode.EQUAL ||
            (splitAmong.isNotEmpty() && kotlin.math.abs(customSum - totalAmount) < 0.01)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KippoColors.Teal,
        unfocusedBorderColor = KippoColors.DarkTeal.copy(alpha = 0.3f),
        focusedLabelColor = KippoColors.Teal,
        unfocusedLabelColor = KippoColors.DarkText.copy(alpha = 0.6f),
        cursorColor = KippoColors.Teal,
        focusedTextColor = KippoColors.DarkText,
        unfocusedTextColor = KippoColors.DarkText
    )
    val receiptPreview = remember(receiptImageBase64) {
        ImageUtils.decodeToImageBitmapOrNull(receiptImageBase64)
    }
    val receiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            receiptImageBase64 = ImageUtils.uriToBase64(context, uri)
        }
    }
    val receiptCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            receiptImageBase64 = ImageUtils.bitmapToBase64(bitmap)
        }
    }
    if (showPhotoOptionsSheet) {
        PhotoSourceBottomSheet(
            onDismiss = { showPhotoOptionsSheet = false },
            onTakePhoto = { receiptCameraLauncher.launch(null) },
            onSelectPhoto = { receiptPickerLauncher.launch("image/*") }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(vertical = 24.dp),
        title = {
            Text("Add expense", fontWeight = FontWeight.Bold, color = KippoColors.DarkText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Title") },
                    placeholder = { Text("E.g.: Friday dinner") },
                    isError = titleError,
                    supportingText = if (titleError) { { Text("Title is required") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; amountError = false },
                    label = { Text("Amount (€)") },
                    isError = amountError,
                    supportingText = if (amountError) { { Text("Enter a valid amount") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                // Paid by
                Column {
                    Text(
                        "Paid by",
                        style = MaterialTheme.typography.labelMedium,
                        color = KippoColors.DarkText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Box {
                        OutlinedButton(
                            onClick = { paidByExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KippoColors.DarkText),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KippoColors.DarkTeal.copy(alpha = 0.3f))
                        ) {
                            val paidByName = members.find { it.uid == paidBy }?.username ?: "Me"
                            Text(paidByName, color = KippoColors.Teal, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = paidByExpanded,
                            onDismissRequest = { paidByExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            members.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.username) },
                                    onClick = { paidBy = member.uid; paidByExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Split section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Mode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Split",
                            style = MaterialTheme.typography.labelMedium,
                            color = KippoColors.DarkText.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.weight(1f))
                        FilterChip(
                            selected = splitMode == SplitMode.EQUAL,
                            onClick = { splitMode = SplitMode.EQUAL; customSplitError = false },
                            label = { Text("Equal split", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KippoColors.Teal,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = splitMode == SplitMode.CUSTOM,
                            onClick = { splitMode = SplitMode.CUSTOM; customSplitError = false },
                            label = { Text("By amounts", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KippoColors.Teal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    if (splitMode == SplitMode.EQUAL) {
                        // Checkboxes — igual que antes
                        members.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        splitAmong = if (member.uid in splitAmong)
                                            splitAmong - member.uid
                                        else
                                            splitAmong + member.uid
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = member.uid in splitAmong,
                                    onCheckedChange = {
                                        splitAmong = if (it) splitAmong + member.uid else splitAmong - member.uid
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = KippoColors.Teal)
                                )
                                Text(
                                    member.username,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KippoColors.DarkText
                                )
                                if (totalAmount > 0 && splitAmong.isNotEmpty() && member.uid in splitAmong) {
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        "%.2f €".format(totalAmount / splitAmong.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KippoColors.Teal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        // Campos de importe por persona
                        members.forEach { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(KippoColors.Teal.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        member.username.take(2).uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KippoColors.Teal
                                    )
                                }
                                Text(
                                    member.username,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KippoColors.DarkText
                                )
                                OutlinedTextField(
                                    value = customAmounts[member.uid] ?: "",
                                    onValueChange = { v ->
                                        customAmounts = customAmounts + (member.uid to v)
                                        customSplitError = false
                                    },
                                    placeholder = { Text("0.00") },
                                    suffix = { Text("€") },
                                    modifier = Modifier.width(90.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = fieldColors,
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    )
                                )
                            }
                        }

                        // Indicador de total
                        val sumColor = when {
                            customSum == 0.0 -> KippoColors.DarkText.copy(alpha = 0.4f)
                            customSumOk -> KippoColors.Teal
                            else -> Color(0xFFE53935)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (customSumOk && customSum > 0) KippoColors.Teal.copy(alpha = 0.06f)
                                    else if (!customSumOk && customSum > 0) Color(0xFFFFEBEE)
                                    else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total assigned:",
                                style = MaterialTheme.typography.bodySmall,
                                color = KippoColors.DarkText.copy(alpha = 0.6f)
                            )
                            Text(
                                "%.2f € / %.2f €".format(customSum, totalAmount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = sumColor
                            )
                        }

                        if (customSplitError) {
                            Text(
                                "Total must equal the expense amount (%.2f €)".format(totalAmount),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                }

                // Categoría
                Column {
                    Text(
                        "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = KippoColors.DarkText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ExpenseCategory.entries.toTypedArray()) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.label, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = categoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KippoColors.Teal,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showPhotoOptionsSheet = true },
                        border = BorderStroke(1.dp, KippoColors.Teal.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Attach photo")
                    }
                }
                if (receiptImageBase64 != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { receiptImageBase64 = null }) {
                            Text("Remove")
                        }
                    }
                }
                if (receiptPreview != null) {
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Image(
                            bitmap = receiptPreview,
                            contentDescription = "Receipt preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.replace(",", ".").toDoubleOrNull()
                    titleError = title.isBlank()
                    amountError = amount == null || amount <= 0.0
                    if (splitMode == SplitMode.CUSTOM && !customSumOk) {
                        customSplitError = true
                        return@Button
                    }
                    if (!titleError && !amountError && splitAmong.isNotEmpty()) {
                        val resolvedCustomSplits = if (splitMode == SplitMode.CUSTOM) {
                            customAmounts
                                .filter { it.key in splitAmong }
                                .mapValues { it.value.replace(",", ".").toDoubleOrNull() ?: 0.0 }
                        } else emptyMap()
                        onAdd(
                            title, amount!!, paidBy,
                            splitAmong.toList(),
                            selectedCategory.key, notes,
                            resolvedCustomSplits,
                            receiptImageBase64
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ADD EXPENSE", fontWeight = FontWeight.Bold)
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

@Composable
fun SettleUpDialog(
    fromUid: String,
    toUid: String,
    prefilledAmount: Double,
    members: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (fromUid: String, toUid: String, amount: Double, note: String) -> Unit
) {
    val fromUsername = members.find { it.uid == fromUid }?.username ?: "?"
    val toUsername = members.find { it.uid == toUid }?.username ?: "?"
    var amountText by remember { mutableStateOf("%.2f".format(prefilledAmount)) }
    var note by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KippoColors.Teal,
        unfocusedBorderColor = KippoColors.DarkTeal.copy(alpha = 0.3f),
        focusedLabelColor = KippoColors.Teal,
        cursorColor = KippoColors.Teal,
        focusedTextColor = KippoColors.DarkText,
        unfocusedTextColor = KippoColors.DarkText
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.92f),
        title = {
            Text("Record payment", fontWeight = FontWeight.Bold, color = KippoColors.DarkText)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = KippoColors.Teal.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            fromUsername,
                            fontWeight = FontWeight.Bold,
                            color = KippoColors.DarkText,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = KippoColors.Teal
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            toUsername,
                            fontWeight = FontWeight.Bold,
                            color = KippoColors.DarkText,
                            fontSize = 16.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; amountError = false },
                    label = { Text("Amount (€)") },
                    isError = amountError,
                    supportingText = if (amountError) { { Text("Enter a valid amount") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.replace(",", ".").toDoubleOrNull()
                    amountError = amount == null || amount <= 0.0
                    if (!amountError) {
                        onConfirm(fromUid, toUid, amount!!, note)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CONFIRM PAYMENT", fontWeight = FontWeight.Bold)
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

fun categoryIcon(category: ExpenseCategory): ImageVector = when (category) {
    ExpenseCategory.COMIDA -> Icons.Default.Restaurant
    ExpenseCategory.TRANSPORTE -> Icons.Default.DirectionsCar
    ExpenseCategory.HOGAR -> Icons.Default.Home
    ExpenseCategory.OCIO -> Icons.Default.SportsEsports
    ExpenseCategory.OTRO -> Icons.Default.Category
}
