package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String = "",
    val companyName: String = "",
    val mobileNumber: String = "",
    val whatsAppNumber: String = "",
    val email: String = "",
    val telegramUsername: String = "",
    val telegramId: String = "",
    val website: String = "",
    val address: String = "",
    val socialLinks: String = "",
    val category: String = "",
    val leadScore: String = "Medium", // High, Medium, Low
    val status: String = "New", // New, Contacted, Interested, Follow Up, Converted
    val validationStatus: String = "Needs Review", // Valid, Needs Review, Invalid
    val notes: String = ""
)

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val content: String = "" // Message text with placeholders like {name}
)

@Entity(tableName = "follow_ups")
data class FollowUp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leadId: Long = 0,
    val dueDate: Long = 0,
    val note: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val status: String = "Pending", // Pending, Processing, Completed, Failed
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(entity = Campaign::class, parentColumns = ["id"], childColumns = ["campaignId"]),
        ForeignKey(entity = Lead::class, parentColumns = ["id"], childColumns = ["leadId"])
    ],
    indices = [Index("campaignId"), Index("leadId")]
)
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val campaignId: Long,
    val leadId: Long,
    val channel: String, // "email", "telegram"
    val content: String = "",
    val status: String = "Pending", // Pending, Sending, Sent, Failed
    val scheduledTime: Long = System.currentTimeMillis(),
    val sentTime: Long? = null,
    val errorMessage: String? = null
)
