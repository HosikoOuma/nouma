package com.nkds.hosikoouma.nouma.features.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nkds.hosikoouma.nouma.R
import com.nkds.hosikoouma.nouma.data.FontChoice
import com.nkds.hosikoouma.nouma.data.Language
import com.nkds.hosikoouma.nouma.data.Theme
import com.nkds.hosikoouma.nouma.utils.copyUriToInternalStorage
import com.nkds.hosikoouma.nouma.utils.createAvatarImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onLogoutClick: () -> Unit
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeveloperSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsViewModel.loadCurrentUser()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            settingsViewModel.clearError()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val internalUri = copyUriToInternalStorage(context, it)
                settingsViewModel.onTempAvatarUriChanged(internalUri)
            }
        }
    )

    if (showDeveloperSheet) {
        ModalBottomSheet(onDismissRequest = { showDeveloperSheet = false }) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = stringResource(R.string.settings_developer),
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.settings_developer), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.developer_name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.developer_telegram_label), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.developer_telegram), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Profile Card
        Card(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable(enabled = uiState.isEditing) { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarToShow = uiState.tempAvatarUri ?: uiState.user?.avatarUri?.let { Uri.parse(it) }
                    if (avatarToShow != null) {
                        AsyncImage(
                            model = createAvatarImageRequest(context, avatarToShow),
                            contentDescription = stringResource(R.string.settings_avatar),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        uiState.user?.username?.firstOrNull()?.let {
                            Text(it.toString(), fontSize = 60.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isEditing) {
                    OutlinedTextField(uiState.tempNickname, { settingsViewModel.onTempNicknameChanged(it) }, label = { Text(stringResource(R.string.settings_nickname)) })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(uiState.tempUsername, { settingsViewModel.onTempUsernameChanged(it) }, label = { Text(stringResource(R.string.settings_username_login)) })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(uiState.tempEmail, { settingsViewModel.onTempEmailChanged(it) }, label = { Text(stringResource(R.string.settings_email)) })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(uiState.tempBio, { settingsViewModel.onTempBioChanged(it) }, label = { Text(stringResource(R.string.settings_bio)) }, maxLines = 3)
                } else {
                    uiState.user?.let {
                        Text(it.nickname ?: it.username, style = MaterialTheme.typography.headlineSmall)
                        Text("@${it.username}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(it.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.user_id, it.id), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it.bio ?: stringResource(R.string.settings_no_bio), style = MaterialTheme.typography.bodyMedium, fontStyle = if (it.bio == null) FontStyle.Italic else FontStyle.Normal)
                    }
                }
            }
        }

        // Appearance Settings Card
        Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                var themeMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = themeMenuExpanded, onExpandedChange = { themeMenuExpanded = !themeMenuExpanded }) {
                    OutlinedTextField(
                        value = uiState.selectedTheme.getDisplayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_theme)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = themeMenuExpanded, onDismissRequest = { themeMenuExpanded = false }) {
                        Theme.values().forEach { theme ->
                            DropdownMenuItem(text = { Text(theme.getDisplayName()) }, onClick = {
                                settingsViewModel.onThemeChange(theme)
                                themeMenuExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var fontMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = fontMenuExpanded, onExpandedChange = { fontMenuExpanded = !fontMenuExpanded }) {
                    OutlinedTextField(
                        value = uiState.selectedFont.getDisplayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_font)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = fontMenuExpanded, onDismissRequest = { fontMenuExpanded = false }) {
                        FontChoice.values().forEach { font ->
                            DropdownMenuItem(text = { Text(font.getDisplayName()) }, onClick = {
                                settingsViewModel.onFontChange(font)
                                fontMenuExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var languageMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = languageMenuExpanded, onExpandedChange = { languageMenuExpanded = !languageMenuExpanded }) {
                    OutlinedTextField(
                        value = uiState.selectedLanguage.getDisplayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = languageMenuExpanded, onDismissRequest = { languageMenuExpanded = false }) {
                        Language.values().forEach { language ->
                            DropdownMenuItem(text = { Text(language.getDisplayName()) }, onClick = {
                                settingsViewModel.onLanguageChange(language)
                                languageMenuExpanded = false
                            })
                        }
                    }
                }
            }
        }

        // Developer Info Card
        Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeveloperSheet = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.settings_developer), style = MaterialTheme.typography.titleMedium)
            }
        }

        // Logout Button Card
        Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onLogoutClick).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.settings_logout), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
