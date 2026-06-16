package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.Lead
import com.example.ui.OutreachHelper
import com.example.ui.OutreachViewModel
import kotlinx.coroutines.launch

data class MarketingChannel(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChannelOutreachScreen(
    leadId: Long,
    viewModel: OutreachViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var lead by remember { mutableStateOf<Lead?>(null) }
    var message by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val settings by viewModel.appSettings.collectAsState()

    LaunchedEffect(leadId) {
        lead = viewModel.getLeadById(leadId)
        message = lead?.let { "Hi ${it.name}, I noticed your business ${it.business}. Would love to connect!" } ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-Channel Broadcast") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        lead?.let { currentLead ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Broadcast Message", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isGenerating = true
                                    message = viewModel.personalizeMessage(currentLead, message)
                                    isGenerating = false
                                }
                            },
                            enabled = !isGenerating,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            if (isGenerating) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            else {
                                Icon(Icons.Default.AutoAwesome, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Personalize")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Channels to Trigger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                val isApiConfigured = settings.whatsappToken.isNotEmpty() || settings.telegramBotToken.isNotEmpty()
                if (!isApiConfigured) {
                    Text(
                        "Automation APIs not configured. Go to Settings to enable background broadcast.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val channels = listOf(
                    MarketingChannel("wa", "WhatsApp", Icons.Default.Chat) { OutreachHelper.sendWhatsApp(context, currentLead, message) },
                    MarketingChannel("sms", "SMS / iMessage", Icons.Default.Sms) { OutreachHelper.sendSms(context, currentLead, message) },
                    MarketingChannel("tg", "Telegram", Icons.AutoMirrored.Filled.Send) { OutreachHelper.openTelegram(context, currentLead) },
                    MarketingChannel("email", "Email", Icons.Default.Email) { OutreachHelper.sendEmail(context, currentLead, "Inquiry", message) },
                    MarketingChannel("share", "Global Share (All Apps)", Icons.Default.Share) { OutreachHelper.shareToGeneric(context, message) }
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(channels) { channel ->
                        OutlinedCard(
                            onClick = channel.action,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(channel.icon, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(channel.name, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Filled.Launch, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        // Bulk logic can be added here if Business APIs are available
                        OutreachHelper.shareToGeneric(context, message)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Trigger Global Multi-App Intent")
                }
            }
        }
    }
}
