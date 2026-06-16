package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.models.Campaign
import com.example.data.models.Message
import com.example.ui.OutreachViewModel
import com.example.worker.BroadcastWorker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastDashboardScreen(
    viewModel: OutreachViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allLeads by viewModel.allLeads.collectAsState()
    val allCampaigns by viewModel.allCampaigns.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var selectedLeads = remember { mutableStateListOf<Long>() }
    var selectedChannel by remember { mutableStateOf("telegram") }
    var campaignName by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Broadcast Dashboard") }) },
        floatingActionButton = {
            if (selectedLeads.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (campaignName.isBlank()) return@ExtendedFloatingActionButton
                        scope.launch {
                            isSending = true
                            
                            val campaign = Campaign(name = campaignName)
                            val campaignId = viewModel.insertCampaign(campaign)

                            selectedLeads.forEach { leadId ->
                                val lead = allLeads.find { it.id == leadId } ?: return@forEach
                                val baseTemplate = allTemplates.firstOrNull()?.content ?: "Hello {name}, ..."
                                val personalized = viewModel.personalizeMessage(lead, baseTemplate)
                                
                                val message = Message(
                                    campaignId = campaignId,
                                    leadId = leadId,
                                    channel = selectedChannel,
                                    content = personalized,
                                    status = "Pending"
                                )
                                val messageId = viewModel.insertMessage(message)

                                val inputData = Data.Builder()
                                    .putLong("messageId", messageId)
                                    .build()

                                val workRequest = OneTimeWorkRequestBuilder<BroadcastWorker>()
                                    .setInputData(inputData)
                                    .build()

                                WorkManager.getInstance(context).enqueue(workRequest)
                            }
                            isSending = false
                            selectedLeads.clear()
                            campaignName = ""
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, "Send Campaign") },
                    text = { Text("Send to ${selectedLeads.size} Leads") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Create Campaign", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = campaignName,
                onValueChange = { campaignName = it },
                label = { Text("Campaign Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Template", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (allTemplates.isEmpty()) {
                Text("Please create a template first", color = MaterialTheme.colorScheme.error)
            } else {
                Text("Using standard template: ${allTemplates.firstOrNull()?.name}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Channel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedChannel == "telegram", onClick = { selectedChannel = "telegram" }, label = { Text("Telegram") })
                FilterChip(selected = selectedChannel == "email", onClick = { selectedChannel = "email" }, label = { Text("Email") })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Select Leads", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allLeads) { lead ->
                    val isSelected = selectedLeads.contains(lead.id)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            if (isSelected) selectedLeads.remove(lead.id)
                            else selectedLeads.add(lead.id)
                        }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(lead.name, fontWeight = FontWeight.Bold)
                                Text(lead.business, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (allCampaigns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent Campaigns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allCampaigns) { campaign ->
                        ListItem(
                            headlineContent = { Text(campaign.name) },
                            supportingContent = { Text("Status: ${campaign.status}") }
                        )
                    }
                }
            }
        }
    }
}
