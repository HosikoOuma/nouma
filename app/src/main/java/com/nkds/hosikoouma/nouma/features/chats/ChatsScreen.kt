package com.nkds.hosikoouma.nouma.features.chats

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nkds.hosikoouma.nouma.R
import com.nkds.hosikoouma.nouma.data.local.MessageType
import com.nkds.hosikoouma.nouma.utils.createAvatarImageRequest
import com.nkds.hosikoouma.nouma.utils.formatTimestamp
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(chat: ChatUiState, isSelected: Boolean, onItemClick: (Int) -> Unit, onItemLongClick: (Int) -> Unit) {
    val context = LocalContext.current
    val cardColors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp).clip(CardDefaults.shape).combinedClickable(onClick = { onItemClick(chat.chatId) }, onLongClick = { onItemLongClick(chat.chatId) }),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                if (chat.opponentAvatarUri != null) {
                    AsyncImage(
                        model = createAvatarImageRequest(context, Uri.parse(chat.opponentAvatarUri)),
                        contentDescription = chat.opponentName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = chat.opponentName.first().toString(), fontSize = 24.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = chat.opponentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val lastMessageText = when (chat.lastMessage?.type) {
                    MessageType.TEXT -> chat.lastMessage.text
                    MessageType.IMAGE -> stringResource(R.string.chat_media_type_photo)
                    MessageType.VIDEO -> stringResource(R.string.chat_media_type_video)
                    MessageType.FILE -> stringResource(R.string.chat_media_type_file)
                    MessageType.MUSIC -> stringResource(R.string.chat_media_type_music)
                    MessageType.VOICE -> stringResource(R.string.chat_media_type_voice)
                    null -> stringResource(R.string.chat_no_messages)
                }
                Text(text = lastMessageText ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (chat.lastMessage != null) {
                Text(text = formatTimestamp(context, chat.lastMessage.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun NewChatDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_chat_dialog_title)) },
        text = { OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text(stringResource(R.string.new_chat_dialog_username)) }) },
        confirmButton = { Button(onClick = { onConfirm(username) }) { Text(stringResource(R.string.new_chat_dialog_create)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    chatsViewModel: ChatsViewModel, 
    showNewChatDialog: Boolean,
    setShowNewChatDialog: (Boolean) -> Unit,
    onChatClick: (Int) -> Unit
) {
    val chats by chatsViewModel.chatsState.collectAsState()
    val selectedIds by chatsViewModel.selectedChatIds.collectAsState()
    val isSelectionMode = selectedIds.isNotEmpty()
    val showDeleteDialog by chatsViewModel.showDeleteConfirmation.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        chatsViewModel.loadChats()
        chatsViewModel.newChatEvent.collectLatest {
            when (it) {
                is NewChatResult.Success -> onChatClick(it.chatId)
                is NewChatResult.Error -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { setShowNewChatDialog(false) },
            onConfirm = { username ->
                chatsViewModel.onFindUserClick(username)
                setShowNewChatDialog(false)
            }
        )
    }

    if (showDeleteDialog) {
        ModalBottomSheet(onDismissRequest = { chatsViewModel.cancelDeletion() }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(id = R.string.delete_confirmation_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(id = R.string.delete_confirmation_message), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { chatsViewModel.cancelDeletion() }, modifier = Modifier.weight(1f)) { Text(stringResource(id = R.string.action_cancel)) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { chatsViewModel.confirmDeletion() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(stringResource(id = R.string.action_delete)) }
                }
            }
        }
    }
    
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        items(chats, key = { it.chatId }) { chat ->
            ChatItem(
                chat = chat,
                isSelected = selectedIds.contains(chat.chatId),
                onItemClick = { if (isSelectionMode) chatsViewModel.toggleChatSelection(it) else onChatClick(it) },
                onItemLongClick = { chatsViewModel.toggleChatSelection(it) }
            )
        }
    }
}
