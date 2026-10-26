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

    companion object {
        private const val PREFS_NAME = "aira_ai_v12_prefs"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_VOICE_MUTED = "voice_muted"

        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }
}
