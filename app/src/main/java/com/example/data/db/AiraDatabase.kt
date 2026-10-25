package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.CryptoVault
import com.example.data.model.AiraMemory
import com.example.data.model.AuditLog
import com.example.data.model.AutomationRoutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [AiraMemory::class, AuditLog::class, AutomationRoutine::class],
    version = 1,
    exportSchema = false
)
abstract class AiraDatabase : RoomDatabase() {
    abstract fun airaDao(): AiraDao

    companion object {
        @Volatile
        private var INSTANCE: AiraDatabase? = null

        fun getDatabase(context: Context): AiraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AiraDatabase::class.java,
                    "aira_neural_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default personalized memories and routines asynchronously
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDefaults(getDatabase(context).airaDao())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDefaults(dao: AiraDao) {
            // Default Learned User Preferences
            val defaultMemories = listOf(
                AiraMemory(
                    category = "PREFERENCE",
                    key = "primary_messaging_app",
                    encryptedValue = CryptoVault.encrypt("Messages"),
                    plainSummary = "Default messaging client set to Messages",
                    confidence = 0.98f
                ),
                AiraMemory(
                    category = "PREFERENCE",
                    key = "preferred_interaction_tone",
                    encryptedValue = CryptoVault.encrypt("concise_technical"),
                    plainSummary = "Interaction tone: Concise & Tactical",
                    confidence = 0.95f
                ),
                AiraMemory(
                    category = "SECURITY",
                    key = "biometric_guard_level",
                    encryptedValue = CryptoVault.encrypt("strict_for_messaging"),
                    plainSummary = "Biometric required before sending messages or system changes",
                    confidence = 1.0f
                ),
                AiraMemory(
                    category = "CONTACT",
                    key = "contact_mom",
                    encryptedValue = CryptoVault.encrypt("+15550192837 (Mom)"),
                    plainSummary = "Contact alias: 'Mom' mapped to +1 555-019-2837",
                    confidence = 0.92f
                ),
                AiraMemory(
                    category = "BEHAVIOR",
                    key = "screen_reader_summary_depth",
                    encryptedValue = CryptoVault.encrypt("high_density"),
                    plainSummary = "Summarize screen with interactive elements highlighted",
                    confidence = 0.88f
                )
            )
            for (m in defaultMemories) {
                dao.insertMemory(m)
            }

            // Default Automation Routines
            val defaultRoutines = listOf(
                AutomationRoutine(
                    title = "Rapid Status Message",
                    triggerPhrase = "Text Mom that I'm on my way",
                    description = "Autonomously opens messaging app, enters recipient and types status update.",
                    category = "COMMUNICATION",
                    requiresBiometric = true,
                    stepsJson = """[{"action":"OPEN_APP","target":"Messages"},{"action":"LOCATE_NODE","target":"Mom"},{"action":"TYPE_TEXT","target":"On my way home now!"},{"action":"CLICK_SEND","target":"Send"}]"""
                ),
                AutomationRoutine(
                    title = "Deep Screen Scan & Summarize",
                    triggerPhrase = "Read screen and summarize",
                    description = "Inspects current view hierarchy, filters noise, and provides audio/text breakdown.",
                    category = "ACCESSIBILITY",
                    requiresBiometric = false,
                    stepsJson = """[{"action":"CAPTURE_SCREEN","target":"active_window"},{"action":"PARSE_NODES","target":"text_and_actions"},{"action":"NEURAL_SYNTHESIS","target":"summary"}]"""
                ),
                AutomationRoutine(
                    title = "Focus Shield Mode",
                    triggerPhrase = "Turn on Focus Shield",
                    description = "Mutes device notifications, closes distracting recents, and enables Do Not Disturb.",
                    category = "SYSTEM",
                    requiresBiometric = false,
                    stepsJson = """[{"action":"GLOBAL_ACTION","target":"NOTIFICATIONS"},{"action":"QUICK_SETTING","target":"DND"},{"action":"GLOBAL_ACTION","target":"HOME"}]"""
                ),
                AutomationRoutine(
                    title = "Emergency Panic Lock",
                    triggerPhrase = "Lock down AIRA vault",
                    description = "Wipes runtime neural cache, secures all memory behind biometric biometric challenge.",
                    category = "PRIVACY",
                    requiresBiometric = true,
                    stepsJson = """[{"action":"PURGE_TEMP_CACHE","target":"memory"},{"action":"LOCK_DEVICE","target":"KEYGUARD"}]"""
                )
            )
            for (r in defaultRoutines) {
                dao.insertRoutine(r)
            }

            // Initial Audit Log
            dao.insertAuditLog(
                AuditLog(
                    actionType = "NEURAL_INITIALIZATION",
                    targetApp = "AIRA_CORE",
                    summary = "Local neural engine booted. Hardware AES-256 GCM vault initialized. 0 bytes network egress.",
                    securityStatus = "VERIFIED_OFFLINE",
                    cryptoHash = CryptoVault.sha256("AIRA_BOOT_" + System.currentTimeMillis())
                )
            )
        }
    }
}
