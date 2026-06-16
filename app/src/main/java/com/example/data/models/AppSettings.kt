package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val whatsappToken: String = "",
    val whatsappPhoneNumberId: String = "",
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val gmailToken: String = "",
    val hasCompletedOnboarding: Boolean = false
)
