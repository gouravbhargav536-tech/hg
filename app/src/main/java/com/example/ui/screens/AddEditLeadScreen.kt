package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.Lead
import com.example.ui.OutreachViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLeadScreen(
    leadId: Long?,
    viewModel: OutreachViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
    var business by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("New") }
    var notes by remember { mutableStateOf("") }

    val statusOptions = listOf("New", "Contacted", "Interested", "Closed")

    LaunchedEffect(leadId) {
        if (leadId != null && leadId != -1L) {
            val lead = viewModel.getLeadById(leadId)
            lead?.let {
                name = it.name
                phone = it.phone
                email = it.email
                telegram = it.telegramId
                business = it.business
                category = it.category
                status = it.status
                notes = it.notes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (leadId == null || leadId == -1L) "Add New Lead" else "Edit Lead") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) return@ExtendedFloatingActionButton
                    val lead = Lead(
                        id = if (leadId != null && leadId != -1L) leadId else 0,
                        name = name,
                        phone = phone,
                        email = email,
                        telegramId = telegram,
                        business = business,
                        category = category,
                        status = status,
                        notes = notes
                    )
                    if (leadId == null || leadId == -1L) {
                        viewModel.insertLead(lead)
                    } else {
                        viewModel.updateLead(lead)
                    }
                    onBack()
                },
                icon = { Icon(Icons.Default.Save, null) },
                text = { Text("Save Lead") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name*") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number*") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = telegram, onValueChange = { telegram = it }, label = { Text("Telegram Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = business, onValueChange = { business = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Real Estate)") }, modifier = Modifier.fillMaxWidth())

            Text("Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                statusOptions.forEach { option ->
                    FilterChip(
                        selected = status == option,
                        onClick = { status = option },
                        label = { Text(option) }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
