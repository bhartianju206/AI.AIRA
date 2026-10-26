package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AiraPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getApiKey(): String {
        return prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    }

    fun setApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, apiKey.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun isVoiceMuted(): Boolean {
        return prefs.getBoolean(KEY_VOICE_MUTED, false)
    }

    fun setVoiceMuted(muted: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_MUTED, muted).apply()
    }

    fun saveSessions(sessions: List<com.example.data.model.ChatSession>) {
        try {
            val root = org.json.JSONArray()
            for (session in sessions) {
                val sessionObj = org.json.JSONObject()
                sessionObj.put("id", session.id)
                sessionObj.put("title", session.title)
                sessionObj.put("timestamp", session.timestamp)
                val msgArr = org.json.JSONArray()
                for (msg in session.messages) {
                    val msgObj = org.json.JSONObject()
                    msgObj.put("id", msg.id)
                    msgObj.put("sender", msg.sender.name)
                    msgObj.put("text", msg.text)
                    msgObj.put("timestamp", msg.timestamp)
                    msgObj.put("actionSummary", msg.actionSummary ?: "")
                    msgObj.put("actionType", msg.actionType ?: "")
                    msgObj.put("isError", msg.isError)
                    msgObj.put("canRetry", msg.canRetry)
                    msgObj.put("hasScreenAttachment", msg.hasScreenAttachment)
                    msgArr.put(msgObj)
                }
                sessionObj.put("messages", msgArr)
                root.put(sessionObj)
            }
            prefs.edit().putString(KEY_CHAT_SESSIONS, root.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun loadSessions(): List<com.example.data.model.ChatSession> {
        val raw = prefs.getString(KEY_CHAT_SESSIONS, null) ?: return emptyList()
        return try {
            val root = org.json.JSONArray(raw)
            val list = mutableListOf<com.example.data.model.ChatSession>()
            for (i in 0 until root.length()) {
                val sessionObj = root.getJSONObject(i)
                val id = sessionObj.optString("id", java.util.UUID.randomUUID().toString())
                val title = sessionObj.optString("title", "New Chat")
                val timestamp = sessionObj.optLong("timestamp", System.currentTimeMillis())
                val msgArr = sessionObj.optJSONArray("messages")
                val messages = mutableListOf<com.example.data.model.ChatMessage>()
                if (msgArr != null) {
                    for (j in 0 until msgArr.length()) {
                        val m = msgArr.getJSONObject(j)
                        val senderName = m.optString("sender", "USER")
                        val sender = if (senderName == "AIRA") com.example.data.model.MessageSender.AIRA else com.example.data.model.MessageSender.USER
                        messages.add(
                            com.example.data.model.ChatMessage(
                                id = m.optString("id", java.util.UUID.randomUUID().toString()),
                                sender = sender,
                                text = m.optString("text", ""),
                                timestamp = m.optLong("timestamp", System.currentTimeMillis()),
                                actionSummary = m.optString("actionSummary", "").ifBlank { null },
                                actionType = m.optString("actionType", "").ifBlank { null },
                                isError = m.optBoolean("isError", false),
                                canRetry = m.optBoolean("canRetry", false),
                                hasScreenAttachment = m.optBoolean("hasScreenAttachment", false)
                            )
                        )
                    }
                }
                list.add(com.example.data.model.ChatSession(id = id, title = title, timestamp = timestamp, messages = messages))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getLastActiveSessionId(): String {
        return prefs.getString(KEY_ACTIVE_SESSION_ID, "") ?: ""
    }

    fun setLastActiveSessionId(id: String) {
        prefs.edit().putString(KEY_ACTIVE_SESSION_ID, id).apply()
    }

    companion object {
        private const val PREFS_NAME = "aira_ai_v12_prefs"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_VOICE_MUTED = "voice_muted"
        private const val KEY_CHAT_SESSIONS = "chat_sessions_v12"
        private const val KEY_ACTIVE_SESSION_ID = "active_session_id"

        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }
}
