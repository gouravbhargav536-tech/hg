package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.data.models.FollowUp
import com.example.data.models.Lead
import com.example.data.models.Template
import com.example.ui.OutreachHelper
import com.example.ui.OutreachViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    leadId: Long,
    viewModel: OutreachViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onBroadcast: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var lead by remember { mutableStateOf<Lead?>(null) }
    val templates by viewModel.allTemplates.collectAsState(emptyList())

    val sheetState = rememberModalBottomSheetState()
    var showTemplates by remember { mutableStateOf(false) }

    LaunchedEffect(leadId) {
        lead = viewModel.getLeadById(leadId)
    }

    if (showTemplates && lead != null) {
        ModalBottomSheet(
            onDismissRequest = { showTemplates = false },
            sheetState = sheetState
        ) {
            TemplateSelectionSheet(
                lead = lead!!,
                templates = templates,
                onTemplateSelected = { content, useAi ->
                    scope.launch {
                        val finalMsg = if (useAi) {
                            viewModel.personalizeMessage(lead!!, content)
                        } else {
                            OutreachHelper.formatTemplate(content, lead!!)
                        }
                        OutreachHelper.sendWhatsApp(context, lead!!, finalMsg)
                        viewModel.updateLead(lead!!.copy(lastContacted = System.currentTimeMillis(), status = "Contacted"))
                        showTemplates = false
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lead?.name ?: "Lead Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { lead?.let { onEdit(it.id) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        lead?.let { currentLead ->
            var insights by remember { mutableStateOf<String?>(null) }
            var strategy by remember { mutableStateOf<String?>(null) }
            var loadingInsights by remember { mutableStateOf(false) }
            var loadingStrategy by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(Icons.Default.Phone, "Phone", currentLead.phone)
                        DetailRow(Icons.Default.Email, "Email", currentLead.email)
                        if (currentLead.telegramId.isNotEmpty()) {
                            DetailRow(Icons.Default.Send, "Telegram", currentLead.telegramId)
                        }
                        DetailRow(Icons.Default.Business, "Business", currentLead.business.ifEmpty { "N/A" })
                        DetailRow(Icons.Default.Category, "Category", currentLead.category.ifEmpty { "Uncategorized" })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Gemini Intelligence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                loadingInsights = true
                                insights = viewModel.searchBusinessInsights(currentLead)
                                loadingInsights = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !loadingInsights
                    ) {
                        if (loadingInsights) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Insights")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                loadingStrategy = true
                                strategy = viewModel.analyzeLeadComplexity(currentLead)
                                loadingStrategy = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !loadingStrategy
                    ) {
                        if (loadingStrategy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Strategy")
                        }
                    }
                }

                if (insights != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Business Insights (Powered by Search Grounding)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(insights!!, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (strategy != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Outreach Strategy (Thinking Mode)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(strategy!!, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton(
                        icon = Icons.Default.Chat,
                        label = "WhatsApp",
                        modifier = Modifier.weight(1f),
                        onClick = { showTemplates = true }
                    )
                    ActionButton(
                        icon = Icons.Default.Campaign,
                        label = "Broadcast",
                        modifier = Modifier.weight(1f),
                        onClick = { onBroadcast(currentLead.id) }
                    )
                    ActionButton(
                        icon = Icons.Default.Mail,
                        label = "Email",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            OutreachHelper.sendEmail(context, currentLead, "Hello from Smart Outreach", "")
                        }
                    )
                    if (currentLead.telegramId.isNotEmpty()) {
                        ActionButton(
                            icon = Icons.Default.Send,
                            label = "Telegram",
                            modifier = Modifier.weight(1f),
                            onClick = { OutreachHelper.openTelegram(context, currentLead) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Follow-ups", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        viewModel.insertFollowUp(FollowUp(
                            leadId = currentLead.id,
                            dueDate = System.currentTimeMillis() + 86400000, // +24 hours
                            note = "Follow up with ${currentLead.name}"
                        ))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Notifications, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add 24h Reminder")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = currentLead.notes.ifEmpty { "No notes added yet." },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, size = 20.dp, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun Icon(icon: ImageVector, contentDescription: String?, size: Dp, tint: Color) {
    androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = tint
    )
}

@Composable
fun ActionButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun TemplateSelectionSheet(
    lead: Lead,
    templates: List<Template>,
    onTemplateSelected: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text("Select Template", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (templates.isEmpty()) {
            Text("No templates found. Using default greeting.", color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onTemplateSelected("Hi ${lead.name}, nice to connect!", false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Default")
            }
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(templates) { template ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(template.name, fontWeight = FontWeight.Bold)
                            Text(template.content, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onTemplateSelected(template.content, false) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Send")
                                }
                                FilledTonalButton(
                                    onClick = { onTemplateSelected(template.content, true) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Decorate")
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
