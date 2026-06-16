package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.AppSettings
import com.example.ui.OutreachViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: OutreachViewModel) {
    val settings by viewModel.appSettings.collectAsState()
    
    var whatsappToken by remember { mutableStateOf("") }
    var whatsappId by remember { mutableStateOf("") }
    var telegramToken by remember { mutableStateOf("") }
    var telegramChatId by remember { mutableStateOf("") }
    var gmailToken by remember { mutableStateOf("") }

    LaunchedEffect(settings) {
        whatsappToken = settings.whatsappToken
        whatsappId = settings.whatsappPhoneNumberId
        telegramToken = settings.telegramBotToken
        telegramChatId = settings.telegramChatId
        gmailToken = settings.gmailToken
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurations") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.saveSettings(
                    AppSettings(
                        whatsappToken = whatsappToken,
                        whatsappPhoneNumberId = whatsappId,
                        telegramBotToken = telegramToken,
                        telegramChatId = telegramChatId,
                        gmailToken = gmailToken
                    )
                )
            }) {
                Icon(Icons.Default.Save, contentDescription = "Save Settings")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsCategory(title = "WhatsApp Cloud API") {
                OutlinedTextField(
                    value = whatsappToken,
                    onValueChange = { whatsappToken = it },
                    label = { Text("Access Token") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Key, null) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = whatsappId,
                    onValueChange = { whatsappId = it },
                    label = { Text("Phone Number ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SettingsCategory(title = "Telegram Bot API") {
                OutlinedTextField(
                    value = telegramToken,
                    onValueChange = { telegramToken = it },
                    label = { Text("Bot Token") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Key, null) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = telegramChatId,
                    onValueChange = { telegramChatId = it },
                    label = { Text("Default Chat ID (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SettingsCategory(title = "Gmail / Google OAuth") {
                OutlinedTextField(
                    value = gmailToken,
                    onValueChange = { gmailToken = it },
                    label = { Text("OAuth / App Password") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Key, null) }
                )
                Text(
                    "Note: In production, use official Auth flows. This field supports temporary tokens or App Passwords for dev testing.",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SettingsCategory(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}
