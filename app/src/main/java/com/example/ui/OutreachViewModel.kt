package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.models.AppSettings
import com.example.data.models.Campaign
import com.example.data.models.FollowUp
import com.example.data.models.Lead
import com.example.data.models.Message
import com.example.data.models.Template
import com.example.data.remote.Content
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Part
import com.example.data.remote.RetrofitClient
import com.example.data.remote.Tool
import com.example.data.remote.GenerationConfig
import com.example.data.remote.ThinkingConfig
import com.example.data.repository.OutreachRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class OutreachViewModel(private val repository: OutreachRepository) : ViewModel() {

    val allLeads: StateFlow<List<Lead>> = repository.allLeads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTemplates: StateFlow<List<Template>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingFollowUps: StateFlow<List<FollowUp>> = repository.pendingFollowUps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCampaigns: StateFlow<List<Campaign>> = repository.allCampaigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettings> = repository.appSettings
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun saveSettings(settings: AppSettings) = viewModelScope.launch {
        repository.saveSettings(settings)
    }

    fun insertLead(lead: Lead) = viewModelScope.launch {
        repository.insertLead(lead)
    }

    fun updateLead(lead: Lead) = viewModelScope.launch {
        repository.updateLead(lead)
    }

    fun deleteLead(lead: Lead) = viewModelScope.launch {
        repository.deleteLead(lead)
    }

    fun insertTemplate(template: Template) = viewModelScope.launch {
        repository.insertTemplate(template)
    }

    suspend fun insertCampaign(campaign: Campaign): Long = repository.insertCampaign(campaign)
    
    suspend fun insertMessage(message: Message): Long = repository.insertMessage(message)
    
    fun updateMessage(message: Message) = viewModelScope.launch {
        repository.updateMessage(message)
    }

    fun updateTemplate(template: Template) = viewModelScope.launch {
        repository.updateTemplate(template)
    }

    fun deleteTemplate(template: Template) = viewModelScope.launch {
        repository.deleteTemplate(template)
    }

    fun insertFollowUp(followUp: FollowUp) = viewModelScope.launch {
        repository.insertFollowUp(followUp)
    }

    fun updateFollowUp(followUp: FollowUp) = viewModelScope.launch {
        repository.updateFollowUp(followUp)
    }

    fun deleteFollowUp(followUp: FollowUp) = viewModelScope.launch {
        repository.deleteFollowUp(followUp)
    }
    
    suspend fun personalizeMessage(lead: Lead, templateContent: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext templateContent
        }

        val prompt = """
            Personalize the following outreach message for a lead.
            Lead Name: ${lead.name}
            Business: ${lead.business}
            Category: ${lead.category}
            Notes: ${lead.notes}
            
            Original Template:
            $templateContent
            
            Make it sound professional yet personal. Keep it concise.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: templateContent
        } catch (e: Exception) {
            e.printStackTrace()
            templateContent
        }
    }

    suspend fun searchBusinessInsights(lead: Lead): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return@withContext "No insights available."

        val prompt = "Find recent news, social media presence, and business activities of '${lead.business}' in the '${lead.category}' category. Provide a brief summary to help with personalized outreach."
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            tools = listOf(Tool(googleSearch = kotlinx.serialization.json.buildJsonObject {}))
        )

        try {
            val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No specific insights found."
        } catch (e: Exception) {
            "Error searching insights: ${e.message}"
        }
    }

    suspend fun analyzeLeadComplexity(lead: Lead): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return@withContext "Strategy analysis not available."

        val prompt = """
            Analyze this lead and suggest a 3-step outreach strategy including optimal timing and message tone.
            Name: ${lead.name}
            Business: ${lead.business}
            Category: ${lead.category}
            Notes: ${lead.notes}
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                thinkingConfig = ThinkingConfig(thinkingLevel = "high")
            )
        )

        try {
            val response = RetrofitClient.service.generateContent("gemini-3.1-pro-preview", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Strategy could not be generated."
        } catch (e: Exception) {
            "Error analyzing lead: ${e.message}"
        }
    }

    suspend fun getLeadById(id: Long): Lead? = repository.getLeadById(id)
}

class OutreachViewModelFactory(private val repository: OutreachRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OutreachViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OutreachViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
