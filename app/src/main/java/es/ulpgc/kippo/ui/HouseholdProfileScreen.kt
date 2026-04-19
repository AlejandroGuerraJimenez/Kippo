package es.ulpgc.kippo.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import es.ulpgc.kippo.model.Household
import es.ulpgc.kippo.model.User
import es.ulpgc.kippo.ui.components.PhotoSourceBottomSheet
import es.ulpgc.kippo.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdProfileScreen(
    household: Household,
    members: List<User>,
    onBack: () -> Unit,
    onUpdateName: (String) -> Unit = {},
    onUpdateImage: (String) -> Unit = {},
    onRemoveMember: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isAdmin = household.creatorId == currentUserId
    var showPhotoOptionsSheet by remember { mutableStateOf(false) }
    val householdImageBitmap = remember(household.imageUrl) {
        ImageUtils.decodeToImageBitmapOrNull(household.imageUrl)
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            ImageUtils.uriToBase64(context, uri)?.let(onUpdateImage)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onUpdateImage(ImageUtils.bitmapToBase64(bitmap))
        }
    }
    if (showPhotoOptionsSheet) {
        PhotoSourceBottomSheet(
            onDismiss = { showPhotoOptionsSheet = false },
            onTakePhoto = { cameraLauncher.launch(null) },
            onSelectPhoto = { imagePickerLauncher.launch("image/*") }
        )
    }
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(household.name) }
    
    var userToRemove by remember { mutableStateOf<User?>(null) }

    // Diálogo para editar nombre
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Household Name") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateName(newName)
                    showEditNameDialog = false
                }) { Text("SAVE") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("CANCEL") }
            }
        )
    }

    // Diálogo de confirmación para eliminar miembro
    if (userToRemove != null) {
        AlertDialog(
            onDismissRequest = { userToRemove = null },
            title = { Text("Remove Member") },
            text = { Text("Are you sure you want to remove ${userToRemove?.username} from this household? They will lose access to all shared data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveMember(userToRemove!!.uid)
                        userToRemove = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("REMOVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToRemove = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Household Profile", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Household photo section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (householdImageBitmap != null) {
                    Image(
                        bitmap = householdImageBitmap,
                        contentDescription = "Household photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = KippoColors.Teal
                    )
                }
                
            }
            if (isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showPhotoOptionsSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = KippoColors.Teal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Photo", fontWeight = FontWeight.Bold)                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = household.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText
                )
                if (isAdmin) {
                    IconButton(onClick = { showEditNameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Join Code Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Invite Code",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = household.joinCode,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = KippoColors.Teal,
                            letterSpacing = 4.sp
                        )
                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(household.joinCode)) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Members Section
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KippoColors.DarkText
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${members.size} active",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(members) { member ->
                    MemberRow(
                        user = member, 
                        isCreator = member.uid == household.creatorId,
                        canRemove = isAdmin && member.uid != currentUserId,
                        onRemove = { userToRemove = member }
                    )
                }
            }
        }
    }
}

@Composable
fun MemberRow(user: User, isCreator: Boolean, canRemove: Boolean, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(KippoColors.Teal.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = KippoColors.Teal)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (isCreator) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = KippoColors.Yellow.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "ADMIN",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = KippoColors.DarkText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = user.email,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        if (canRemove) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.PersonRemove, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}
