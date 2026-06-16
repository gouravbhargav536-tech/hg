package com.example.data.local

import androidx.room.*
import com.example.data.models.FollowUp
import com.example.data.models.Lead
import com.example.data.models.Template
import com.example.data.models.AppSettings
import com.example.data.models.Campaign
import com.example.data.models.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettings)
}

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY name ASC")
    fun getAllLeads(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE id = :id")
    suspend fun getLeadById(id: Long): Lead?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead): Long

    @Update
    suspend fun updateLead(lead: Lead)

    @Delete
    suspend fun deleteLead(lead: Lead)
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<Template>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: Template): Long

    @Update
    suspend fun updateTemplate(template: Template)

    @Delete
    suspend fun deleteTemplate(template: Template)
}

@Dao
interface FollowUpDao {
    @Query("SELECT * FROM follow_ups WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingFollowUps(): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE leadId = :leadId")
    fun getFollowUpsForLead(leadId: Long): Flow<List<FollowUp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUp): Long

    @Update
    suspend fun updateFollowUp(followUp: FollowUp)

    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp)
}

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdTime DESC")
    fun getAllCampaigns(): Flow<List<Campaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE campaignId = :campaignId")
    fun getMessagesForCampaign(campaignId: Long): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): Message?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Update
    suspend fun updateMessage(message: Message)
}
