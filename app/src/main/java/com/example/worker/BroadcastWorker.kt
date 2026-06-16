package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.remote.ResendClient
import com.example.data.remote.ResendEmailRequest
import com.example.data.remote.TelegramClient
import com.example.data.remote.TelegramMessageRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BroadcastWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val messageId = inputData.getLong("messageId", -1L)
        if (messageId == -1L) return@withContext Result.failure()

        val database = AppDatabase.getDatabase(applicationContext)
        val messageDao = database.messageDao()
        val settingsDao = database.settingsDao()
        val leadDao = database.leadDao()

        val settings = settingsDao.getSettings().firstOrNull() ?: return@withContext Result.retry()
        val message = messageDao.getMessageById(messageId) ?: return@withContext Result.failure()
        if (message.status == "Sent") return@withContext Result.success()

        messageDao.updateMessage(message.copy(status = "Sending"))

        val lead = leadDao.getLeadById(message.leadId)
        if (lead == null) {
            messageDao.updateMessage(message.copy(status = "Failed", errorMessage = "Lead not found"))
            return@withContext Result.failure()
        }

        try {
            when (message.channel) {
                "telegram" -> {
                    val token = settings.telegramBotToken
                    val chatId = lead.telegramId.ifEmpty { settings.telegramChatId }
                    if (token.isEmpty() || chatId.isEmpty()) {
                        throw Exception("Telegram credentials missing")
                    }
                    val request = TelegramMessageRequest(chat_id = chatId, text = message.content)
                    TelegramClient.service.sendMessage(token, request)
                }
                "email" -> {
                    val token = settings.gmailToken
                    if (token.isEmpty()) {
                        throw Exception("Resend credentials missing")
                    }
                    val request = ResendEmailRequest(
                        from = "onboarding@resend.dev",
                        to = listOf(lead.email),
                        subject = "Outreach Message",
                        text = message.content
                    )
                    ResendClient.service.sendEmail("Bearer $token", request)
                }
                else -> throw Exception("Unknown channel")
            }
            
            messageDao.updateMessage(message.copy(status = "Sent", sentTime = System.currentTimeMillis()))
            Result.success()
        } catch (e: Exception) {
            messageDao.updateMessage(message.copy(status = "Failed", errorMessage = e.message))
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
