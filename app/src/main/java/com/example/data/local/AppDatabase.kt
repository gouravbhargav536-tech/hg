package com.example.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.models.AppSettings
import com.example.data.models.Campaign
import com.example.data.models.FollowUp
import com.example.data.models.Lead
import com.example.data.models.Message
import com.example.data.models.Template
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Lead::class, Template::class, FollowUp::class, AppSettings::class, Campaign::class, Message::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun templateDao(): TemplateDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun settingsDao(): SettingsDao
    abstract fun campaignDao(): CampaignDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "outreach_db")
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Instance?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.templateDao().insertTemplate(Template(name = "Introduction", content = "Hello {name}, I'm reaching out from {businessName} to discuss how we can help with your {category} needs."))
                                    database.templateDao().insertTemplate(Template(name = "Follow-up", content = "Hi {name}, just checking in on the {businessName} project we discussed earlier. Let me know if you have any questions."))
                                }
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
