package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.models.Lead

object OutreachHelper {
    fun sendWhatsApp(context: Context, lead: Lead, message: String) {
        val phone = lead.phone.replace("+", "").replace("-", "").replace(" ", "")
        val url = "https://wa.me/$phone?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, lead: Lead, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${lead.email}")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    fun openTelegram(context: Context, lead: Lead) {
        if (lead.telegramId.isEmpty()) return
        val username = lead.telegramId.replace("@", "")
        val url = "https://t.me/$username"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Telegram not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, lead: Lead, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${lead.phone}")
            putExtra("sms_body", message)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS app not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareToGeneric(context: Context, message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun formatTemplate(content: String, lead: Lead): String {
        return content.replace("{name}", lead.name)
            .replace("{business}", lead.business)
            .replace("{category}", lead.category)
    }
}
