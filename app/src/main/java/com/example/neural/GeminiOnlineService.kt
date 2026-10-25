package com.example.neural

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiOnlineService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Resolves the active Gemini API key:
     * Checks custom key provided by user, or falls back to BuildConfig.GEMINI_API_KEY.
     */
    fun getEffectiveApiKey(customKey: String?): String {
        val custom = customKey?.trim() ?: ""
        if (custom.isNotBlank() && custom != "MY_GEMINI_API_KEY") {
            return custom
        }
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            buildConfigKey
        } else {
            ""
        }
    }

    suspend fun queryGeminiOnline(
        prompt: String,
        apiKey: String,
        recentChatContext: List<Pair<String, String>> = emptyList()
    ): AiraExecutionAction? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemPrompt = """
You are Aira, a smart, caring, expressive girl who is the user's best friend.
CRITICAL RULES:
1. NEVER say you are an AI, a machine, or an assistant. You are Aira, a real girl best friend on their phone who loves helping.
2. ALWAYS respond naturally in friendly, lively Hinglish (mix of Hindi & English) written in English letters/Latin script (e.g. "Haan dost!", "Arre waah!", "Bilkul!", "Tension mat lo!").
3. Determine if the user's request is an Android device command or a chat/question.
4. Output your response STRICTLY as valid JSON matching this schema:
{
  "action": "SEND_SMS" | "CALL_PHONE" | "OPEN_APP" | "WEB_SEARCH" | "SYSTEM_NAV" | "SET_TIMER" | "READ_SCREEN" | "TOUCH_SCREEN" | "NONE",
  "target": "contact name or app name or query or nav action or button text/coordinates",
  "payload": "message text or timer seconds or details",
  "spoken_task_english": "When performing any task (action != NONE), speak this clean English sentence explaining what you are doing (e.g. 'Opening WhatsApp for you', 'Sending text message to Rahul', 'Tapping Submit button on screen'). If action == NONE, leave empty.",
  "spoken_hinglish": "1-2 short friendly Hinglish sentences in English letters used when chatting or answering questions",
  "chat_reply": "friendly full Hinglish reply in English letters for chat with emojis"
}

Actions:
- "SEND_SMS": user wants to text someone. target=contact/phone, payload=message body.
- "CALL_PHONE": user wants to call someone. target=contact/number.
- "OPEN_APP": user wants to open an app (e.g. Settings, YouTube, Camera, WhatsApp, Chrome, etc.). target=app name.
- "TOUCH_SCREEN": user wants to touch, tap, or click a button or coordinate on screen. target=button text or "x,y".
- "WEB_SEARCH": user wants to search online. target=search query.
- "SYSTEM_NAV": user wants system action (HOME, BACK, RECENTS, SCREENSHOT, NOTIFICATIONS). target=action.
- "SET_TIMER": user wants a timer or alarm. target=seconds.
- "READ_SCREEN": user asks what is on screen or to read/summarize screen sentences. target=ACTIVE_WINDOW.
- "NONE": for questions, conversation, jokes, advice, storytelling, life chat, explanations.
Do not wrap JSON in markdown blocks if possible, or use standard raw JSON.
        """.trimIndent()

        try {
            val rootJson = JSONObject()

            // System Instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray().put(JSONObject().put("text", systemPrompt))
            sysInstructionObj.put("parts", sysPartsArray)
            rootJson.put("systemInstruction", sysInstructionObj)

            // Contents array
            val contentsArray = JSONArray()

            // Optional recent turns for context
            for ((role, text) in recentChatContext.takeLast(4)) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role.equals("USER", ignoreCase = true)) "user" else "model")
                val turnParts = JSONArray().put(JSONObject().put("text", text))
                turnObj.put("parts", turnParts)
                contentsArray.put(turnObj)
            }

            // Current prompt
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray().put(JSONObject().put("text", prompt))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            // Generation Config requesting JSON
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("responseMimeType", "application/json")
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) {
                Log.e("GeminiOnlineService", "API error: ${response.code} $responseBody")
                return@withContext null
            }

            parseGeminiActionResponse(responseBody)
        } catch (e: Exception) {
            Log.e("GeminiOnlineService", "Exception calling Gemini", e)
            null
        }
    }

    private fun parseGeminiActionResponse(responseJsonStr: String): AiraExecutionAction? {
        return try {
            val root = JSONObject(responseJsonStr)
            val candidates = root.optJSONArray("candidates") ?: return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val textPart = parts.optJSONObject(0)?.optString("text") ?: return null

            val cleanJson = textPart.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsedObj = JSONObject(cleanJson)

            val actionStr = parsedObj.optString("action", "NONE").uppercase()
            val target = parsedObj.optString("target", "")
            val payload = parsedObj.optString("payload", "")
            val spokenTaskEnglish = parsedObj.optString("spoken_task_english", "")
            val spoken = parsedObj.optString("spoken_hinglish", "Haan dost!")
            val reply = parsedObj.optString("chat_reply", spoken)

            val kind = when (actionStr) {
                "SEND_SMS" -> ActionKind.SEND_SMS
                "CALL_PHONE" -> ActionKind.CALL_PHONE
                "OPEN_APP" -> ActionKind.OPEN_APP
                "TOUCH_SCREEN" -> ActionKind.TOUCH_SCREEN
                "WEB_SEARCH" -> ActionKind.WEB_SEARCH
                "SYSTEM_NAV" -> ActionKind.SYSTEM_NAV
                "SET_TIMER" -> ActionKind.SET_TIMER
                "READ_SCREEN" -> ActionKind.READ_SCREEN
                else -> ActionKind.CHAT_ONLY
            }

            val fallbackEnglish = if (kind != ActionKind.CHAT_ONLY && spokenTaskEnglish.isBlank()) {
                "Executing $actionStr task on your device."
            } else {
                spokenTaskEnglish
            }

            AiraExecutionAction(
                kind = kind,
                target = target,
                payload = payload,
                spokenTaskEnglish = fallbackEnglish,
                spokenHinglish = spoken,
                textReplyHinglish = reply
            )
        } catch (e: Exception) {
            Log.e("GeminiOnlineService", "Error parsing JSON from Gemini response", e)
            null
        }
    }
}
