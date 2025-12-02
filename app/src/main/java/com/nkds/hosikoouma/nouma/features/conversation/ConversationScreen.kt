package com.nkds.hosikoouma.nouma.features.conversation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.nkds.hosikoouma.nouma.R
import com.nkds.hosikoouma.nouma.coil.CoilImageLoader
import com.nkds.hosikoouma.nouma.data.local.Message
import com.nkds.hosikoouma.nouma.data.local.MessageType
import com.nkds.hosikoouma.nouma.features.player.MusicPlayer
import com.nkds.hosikoouma.nouma.utils.copyUriToInternalStorage
import com.nkds.hosikoouma.nouma.utils.getFileNameFromUri
import com.nkds.hosikoouma.nouma.utils.performVibration
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: Message,
    isFromCurrentUser: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: (Int) -> Unit,
    onMediaClick: (Int) -> Unit,
    onFileClick: (Message) -> Unit,
    onMusicClick: (Message) -> Unit
) {
    val context = LocalContext.current
    val alignment = if (isFromCurrentUser) Alignment.End else Alignment.Start

    val backgroundAlpha = if (isSelected) 0.2f else 0f
    val backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha)

    val bubbleColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection(message.messageId)
                    } else {
                        when (message.type) {
                            MessageType.IMAGE, MessageType.VIDEO -> onMediaClick(message.messageId)
                            MessageType.FILE -> onFileClick(message)
                            MessageType.MUSIC, MessageType.VOICE -> onMusicClick(message)
                            else -> Unit
                        }
                    }
                },
                onLongClick = { onToggleSelection(message.messageId) }
            )
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = alignment) {
            when (message.type) {
                MessageType.TEXT -> {
                    if (message.text != null) {
                        Text(
                            text = message.text,
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(bubbleColor).padding(horizontal = 16.dp, vertical = 8.dp),
                            color = textColor
                        )
                    }
                }
                MessageType.IMAGE -> {
                    if (message.text != null) {
                        AsyncImage(
                            model = Uri.parse(message.text),
                            contentDescription = "Image",
                            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                MessageType.VIDEO -> {
                    if (message.text != null) {
                        Box(
                            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = Uri.parse(message.text),
                                imageLoader = CoilImageLoader.get(context),
                                contentDescription = "Video Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Icon(Icons.Default.PlayArrow, contentDescription = "Video", tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                }
                 MessageType.FILE -> {
                     Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bubbleColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = "File", tint = textColor)
                        Spacer(Modifier.width(8.dp))
                        Text(text = message.fileName ?: "File", color = textColor)
                    }
                }
                MessageType.MUSIC, MessageType.VOICE -> {
                    var albumArt by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

                    LaunchedEffect(message.text) {
                        if (message.text == null || message.type == MessageType.VOICE) return@LaunchedEffect
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(context, Uri.parse(message.text))
                            val art = retriever.embeddedPicture
                            art?.let { albumArt = BitmapFactory.decodeByteArray(it, 0, it.size) }
                        } catch (e: Exception) {
                            albumArt = null
                        } finally {
                            retriever.release()
                        }
                    }

                     Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bubbleColor)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = if (message.type == MessageType.VOICE) Icons.Default.Mic else Icons.Default.MusicNote
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                             if (albumArt != null) {
                                Image(
                                    bitmap = albumArt!!.asImageBitmap(),
                                    contentDescription = "Album Art",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(text = message.fileName ?: "Music", color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMedia: (String, String) -> Unit,
    onNavigateToProfile: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMessageIds by viewModel.selectedMessageIds.collectAsState()
    val isSelectionMode = selectedMessageIds.isNotEmpty()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showAttachmentSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun createTempFile(extension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context.cacheDir
        return File.createTempFile(
            "${extension.uppercase()}_${timeStamp}_",
            ".${extension}",
            storageDir
        )
    }

    var musicMessageToPlay by remember { mutableStateOf<Message?>(null) }
    var showCameraChoiceDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val recordAudioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    val voiceRecorder = remember { VoiceRecorder(context) }

    val isEditMode = uiState.messageToEdit != null

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            tempCameraUri?.let { viewModel.sendImageMessage(it) }
        }
        tempCameraUri = null
    }

    val takeVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            tempCameraUri?.let { viewModel.sendVideoMessage(it) }
        }
        tempCameraUri = null
    }

    fun launchCamera(isVideo: Boolean) {
        val file = createTempFile(if (isVideo) "mp4" else "jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        tempCameraUri = uri
        if (isVideo) {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
            takeVideoLauncher.launch(intent)
        } else {
            takePictureLauncher.launch(uri)
        }
    }

    LaunchedEffect(uiState.messageToEdit) {
        uiState.messageToEdit?.let { text = it.text ?: "" }
    }

    BackHandler(enabled = isSelectionMode || isEditMode) {
        if (isSelectionMode) viewModel.clearSelections()
        if (isEditMode) {
            viewModel.cancelEditing()
            text = ""
        }
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                uris.forEach { uri ->
                    val internalUri = copyUriToInternalStorage(context, uri)
                    if (internalUri != null) {
                        val mimeType = context.contentResolver.getType(uri)
                        if (mimeType?.startsWith("video/") == true) {
                            viewModel.sendVideoMessage(internalUri)
                        } else {
                            viewModel.sendImageMessage(internalUri)
                        }
                    }
                }
            }
            showAttachmentSheet = false
        }
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val fileName = getFileNameFromUri(context, it)
                val internalUri = copyUriToInternalStorage(context, it)
                if (internalUri != null) {
                    viewModel.sendFileMessage(internalUri, fileName)
                }
            }
            showAttachmentSheet = false
        }
    )

    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val fileName = getFileNameFromUri(context, it)
                val internalUri = copyUriToInternalStorage(context, it)
                if (internalUri != null) {
                    viewModel.sendMusicMessage(internalUri, fileName)
                }
            }
            showAttachmentSheet = false
        }
    )

    if (showCameraChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showCameraChoiceDialog = false },
            title = { Text("Камера") },
            text = { Text("Сделать фото или записать видео?") },
            confirmButton = {
                TextButton(onClick = { 
                    if (cameraPermissionState.status.isGranted) {
                        launchCamera(false)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                    showCameraChoiceDialog = false
                }) { Text("Фото") }
            },
            dismissButton = {
                 TextButton(onClick = { 
                    if (cameraPermissionState.status.isGranted) {
                        launchCamera(true)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                    showCameraChoiceDialog = false
                 }) { Text("Видео") }
            }
        )
    }


    if (showAttachmentSheet) {
        ModalBottomSheet(onDismissRequest = { showAttachmentSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                 ListItem(
                    headlineContent = { Text(stringResource(R.string.attachment_camera)) },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showAttachmentSheet = false
                        showCameraChoiceDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.attachment_media)) },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable { mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.attachment_file)) },
                    leadingContent = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                    modifier = Modifier.clickable { filePickerLauncher.launch("*/*") }
                )
                 ListItem(
                    headlineContent = { Text(stringResource(R.string.attachment_music)) },
                    leadingContent = { Icon(Icons.Default.Audiotrack, contentDescription = null) },
                    modifier = Modifier.clickable { musicPickerLauncher.launch("audio/*") }
                )
            }
        }
    }

    if (showDeleteConfirmation) {
         ModalBottomSheet(onDismissRequest = { viewModel.cancelMessageDeletion() }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(id = R.string.delete_confirmation_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(id = R.string.delete_confirmation_message), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { viewModel.cancelMessageDeletion() }, modifier = Modifier.weight(1f)) { Text(stringResource(id = R.string.action_cancel)) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { viewModel.confirmMessageDeletion() }, 
                        modifier = Modifier.weight(1f), 
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(id = R.string.action_delete)) }
                }
            }
        }
    }

    musicMessageToPlay?.let { message ->
        val exoPlayer = remember(message) {
            androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(message.text))
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        }

        DisposableEffect(Unit) {
            onDispose { 
                exoPlayer.release()
                musicMessageToPlay = null
            }
        }

         ModalBottomSheet(onDismissRequest = { musicMessageToPlay = null }) {
            MusicPlayer(exoPlayer = exoPlayer, trackName = message.fileName ?: "")
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text(stringResource(R.string.chats_selected_count, selectedMessageIds.size)) },
                    navigationIcon = {
                        IconButton(onClick = { 
                            viewModel.clearSelections()
                            viewModel.cancelEditing()
                            text = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                        }
                    },
                    actions = {
                        val selectedMessage = uiState.messages.find { it.messageId == selectedMessageIds.firstOrNull() }
                        if (selectedMessageIds.size == 1 && selectedMessage?.type == MessageType.TEXT && selectedMessage.senderId == viewModel.currentUserId) {
                            IconButton(onClick = { viewModel.startEditingMessage() }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                        IconButton(onClick = { viewModel.requestMessageDeletion() }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { 
                        uiState.opponent?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onNavigateToProfile(it.id) }
                            ) {
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                                    if (it.avatarUri != null) {
                                        AsyncImage(model = Uri.parse(it.avatarUri), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(text = (it.nickname ?: it.username).first().toString())
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it.nickname ?: it.username)
                            }
                        }
                    },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.conversation_back)) } }
                )
            }
        },
        bottomBar = {
            Column {
                if (isEditMode) {
                     Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                             Text("Редактирование", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                             Text(uiState.messageToEdit?.text ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            viewModel.cancelEditing()
                            text = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Edit")
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isEditMode) {
                        IconButton(onClick = { showAttachmentSheet = true }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Прикрепить")
                        }
                    }

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        readOnly = isRecording,
                        placeholder = {
                            val placeholderText = when {
                                isRecording -> stringResource(R.string.voice_message_recording)
                                text.isEmpty() && !isEditMode -> stringResource(R.string.voice_message_hold_to_record)
                                else -> stringResource(R.string.conversation_message_hint)
                            }
                            Text(placeholderText)
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    if (text.isNotBlank()) {
                        IconButton(onClick = {
                            if (isEditMode) {
                                viewModel.confirmEditing(text)
                            } else {
                                viewModel.sendTextMessage(text)
                            }
                            text = ""
                        }) {
                            val icon = if (isEditMode) Icons.Default.Done else Icons.AutoMirrored.Filled.Send
                            val description = if (isEditMode) "Confirm edit" else stringResource(R.string.conversation_send)
                            Icon(icon, contentDescription = description)
                        }
                    } else if (!isEditMode) {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        LaunchedEffect(isPressed) {
                            if (isPressed) {
                                if (recordAudioPermissionState.status.isGranted) {
                                    performVibration(context)
                                    isRecording = true
                                    voiceRecorder.startRecording()
                                } else {
                                    recordAudioPermissionState.launchPermissionRequest()
                                }
                            } else {
                                if (isRecording) {
                                    performVibration(context)
                                    isRecording = false
                                    voiceRecorder.stopRecording()?.let { file ->
                                        val uri = Uri.fromFile(file)
                                        val fileName = "Voice message ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}"
                                        viewModel.sendVoiceMessage(uri, fileName)
                                    }
                                }
                            }
                        }

                        val micColor by animateColorAsState(
                            if (isRecording) MaterialTheme.colorScheme.error else LocalContentColor.current,
                            label = "Mic Color"
                        )

                        IconButton(
                            onClick = { /* For accessibility */ },
                            interactionSource = interactionSource
                        ) {
                            Icon(
                                Icons.Default.Mic, 
                                contentDescription = stringResource(R.string.voice_message_hold_to_record),
                                tint = micColor
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(uiState.messages) { message ->
                MessageItem(
                    message = message, 
                    isFromCurrentUser = message.senderId == viewModel.currentUserId,
                    isSelected = selectedMessageIds.contains(message.messageId),
                    isSelectionMode = isSelectionMode,
                    onToggleSelection = { viewModel.toggleMessageSelection(it) },
                    onMediaClick = { messageId -> onNavigateToMedia(chatId, messageId.toString()) },
                    onFileClick = { fileMessage ->
                        try {
                            val file = File(Uri.parse(fileMessage.text).path!!)
                            val fileUri = FileProvider.getUriForFile(
                                context,
                                context.applicationContext.packageName + ".provider",
                                file
                            )
                            val mimeType = context.contentResolver.getType(fileUri)

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(fileUri, mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onMusicClick = { musicMessageToPlay = it }
                )
            }
        }
    }
}
