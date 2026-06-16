package com.example.data.repository

import com.example.data.local.CampaignDao
import com.example.data.local.FollowUpDao
import com.example.data.local.LeadDao
import com.example.data.local.MessageDao
import com.example.data.local.SettingsDao
import com.example.data.local.TemplateDao
import com.example.data.models.AppSettings
import com.example.data.models.Campaign
import com.example.data.models.FollowUp
import com.example.data.models.Lead
import com.example.data.models.Message
import com.example.data.models.Template
import com.example.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.Flow

class OutreachRepository(
    private val leadDao: LeadDao,
    private val templateDao: TemplateDao,
    private val followUpDao: FollowUpDao,
    private val settingsDao: SettingsDao,
    private val campaignDao: CampaignDao,
    private val messageDao: MessageDao,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    val allLeads: Flow<List<Lead>> = leadDao.getAllLeads()
    val allTemplates: Flow<List<Template>> = templateDao.getAllTemplates()
    val pendingFollowUps: Flow<List<FollowUp>> = followUpDao.getPendingFollowUps()
    val appSettings: Flow<AppSettings?> = settingsDao.getSettings()
    val allCampaigns: Flow<List<Campaign>> = campaignDao.getAllCampaigns()

    suspend fun getMessagesForCampaign(campaignId: Long): Flow<List<Message>> = messageDao.getMessagesForCampaign(campaignId)

    suspend fun insertCampaign(campaign: Campaign): Long = campaignDao.insertCampaign(campaign)
    suspend fun insertMessage(message: Message): Long = messageDao.insertMessage(message)
    suspend fun updateMessage(message: Message) = messageDao.updateMessage(message)

    suspend fun saveSettings(settings: AppSettings) = settingsDao.saveSettings(settings)

    suspend fun getLeadById(id: Long): Lead? = leadDao.getLeadById(id)

    suspend fun insertLead(lead: Lead) {
        val id = leadDao.insertLead(lead)
        firestoreRepository.insertLead(lead.copy(id = id))
    }

    suspend fun updateLead(lead: Lead) {
        leadDao.updateLead(lead)
        firestoreRepository.updateLead(lead)
    }

    suspend fun deleteLead(lead: Lead) {
        leadDao.deleteLead(lead)
        firestoreRepository.deleteLead(lead)
    }

    suspend fun insertTemplate(template: Template) {
        val id = templateDao.insertTemplate(template)
        firestoreRepository.insertTemplate(template.copy(id = id))
    }

    suspend fun updateTemplate(template: Template) {
        templateDao.updateTemplate(template)
    }

    suspend fun deleteTemplate(template: Template) {
        templateDao.deleteTemplate(template)
        firestoreRepository.deleteTemplate(template)
    }

    fun getFollowUpsForLead(leadId: Long): Flow<List<FollowUp>> = followUpDao.getFollowUpsForLead(leadId)

    suspend fun insertFollowUp(followUp: FollowUp) {
        val id = followUpDao.insertFollowUp(followUp)
        firestoreRepository.insertFollowUp(followUp.copy(id = id))
    }

    suspend fun updateFollowUp(followUp: FollowUp) {
        followUpDao.updateFollowUp(followUp)
        firestoreRepository.updateFollowUp(followUp)
    }

    suspend fun deleteFollowUp(followUp: FollowUp) {
        followUpDao.deleteFollowUp(followUp)
    }
}
