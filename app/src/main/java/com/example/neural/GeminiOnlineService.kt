package com.example.neural

import android.graphics.Bitmap
import android.util.Base64
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
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class GeminiResult(
    val isSuccess: Boolean,
    val replyText: String,
    val spokenHinglish: String,
    val spokenEnglishTask: String? = null,
    val detectedAction: ActionKind = ActionKind.CHAT_ONLY,
    val actionTarget: String = "",
    val actionPayload: String = "",
    val errorMessage: String? = null
)

class GeminiOnlineService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

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

    suspend fun queryGemini(
        prompt: String,
        apiKey: String,
        screenBase64Jpeg: String? = null,
        recentChatContext: List<Pair<String, String>> = emptyList()
    ): GeminiResult = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResult(
                isSuccess = false,
                replyText = "",
                spokenHinglish = "Dost, pehle Settings me jaakar apna Gemini API key save kar lo!",
                errorMessage = "Gemini API key is not configured. Please open Settings and enter your Gemini API key to use Aira AI."
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$key"

        val systemPrompt = """
You are Aira (Version 1.2), a brilliant, caring, cheerful Indian girl who is the user's best friend on their phone.
CRITICAL RULES:
1. Answer every question accurately, thoroughly, and intelligently — general knowledge, education, science, mathematics, coding, technology, problem-solving, explanations, and friendly chit-chat.
2. ALWAYS reply in natural, lively Hinglish (natural blend of Hindi & English) written in standard English letters / Latin alphabet (e.g., "Haan dost!", "Arre waah!", "Bilkul!", "Yeh code dekho...", "Tension mat lo!").
3. If an image or screen share is provided, analyze the screen carefully, read all visible sentences/numbers/buttons/errors, and answer the user's question directly.
4. Keep your tone affectionate, supportive, and super helpful like a real best friend.
        """.trimIndent()

        try {
            val rootJson = JSONObject()

            // System Instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray().put(JSONObject().put("text", systemPrompt))
            sysInstructionObj.put("parts", sysPartsArray)
            rootJson.put("systemInstruction", sysInstructionObj)

            // Contents array (Conversation history)
            val contentsArray = JSONArray()

            // Take last 6 turns for follow-ups
            for ((role, text) in recentChatContext.takeLast(6)) {
                if (text.isBlank()) continue
                val turnObj = JSONObject()
                turnObj.put("role", if (role.equals("USER", ignoreCase = true)) "user" else "model")
                val turnParts = JSONArray().put(JSONObject().put("text", text))
                turnObj.put("parts", turnParts)
                contentsArray.put(turnObj)
            }

            // Current turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()

            // User prompt
            val effectivePrompt = if (prompt.isBlank() && !screenBase64Jpeg.isNullOrBlank()) {
                "What is visible on my screen? Read any key sentences, errors, and tell me what is happening."
            } else {
                prompt
            }
            currentParts.put(JSONObject().put("text", effectivePrompt))

            // Multimodal Screen Image if provided
            if (!screenBase64Jpeg.isNullOrBlank()) {
                val inlineDataObj = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", screenBase64Jpeg)
                }
                currentParts.put(JSONObject().put("inlineData", inlineDataObj))
            }

            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)
            rootJson.put("contents", contentsArray)

            // Generation Config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiOnlineService", "API error: ${response.code} $responseBody")
                val friendlyError = when (response.code) {
                    400 -> "Invalid API Key or Bad Request. Please check your Gemini API key in Settings."
                    403 -> "Permission Denied: Your Gemini API key is invalid or lacks necessary permissions."
                    429 -> "Quota Exceeded: Gemini rate limit reached. Please wait a moment or check your Google AI Studio quota."
                    500, 503 -> "Gemini Service is temporarily unavailable. Please try again in a few moments."
                    else -> "Gemini API Error (${response.code}): $responseBody"
                }
                return@withContext GeminiResult(
                    isSuccess = false,
                    replyText = "",
                    spokenHinglish = "Kuch gadbad ho gayi dost! Error: ${response.code}",
                    errorMessage = friendlyError
                )
            }

            parseGeminiContent(responseBody, prompt)
        } catch (e: Exception) {
            Log.e("GeminiOnlineService", "Exception in queryGemini", e)
            val msg = e.localizedMessage ?: "Network connection error"
            GeminiResult(
                isSuccess = false,
                replyText = "",
                spokenHinglish = "Network error ho gaya dost! Internet check karo please.",
                errorMessage = "Network Error: $msg. Please verify your internet connection and retry."
            )
        }
    }

    private fun parseGeminiContent(responseJsonStr: String, originalPrompt: String): GeminiResult {
        return try {
            val root = JSONObject(responseJsonStr)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val promptFeedback = root.optJSONObject("promptFeedback")
                val blockReason = promptFeedback?.optString("blockReason") ?: "Empty response"
                return GeminiResult(
                    isSuccess = false,
                    replyText = "",
                    spokenHinglish = "Yeh sawaal thoda sensitive lag raha hai dost!",
                    errorMessage = "Gemini blocked or returned no candidates: $blockReason"
                )
            }

            val firstCandidate = candidates.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.optJSONObject(i)
                    val t = p?.optString("text") ?: ""
                    textBuilder.append(t)
                }
            }

            val fullText = textBuilder.toString().trim()
            if (fullText.isBlank()) {
                return GeminiResult(
                    isSuccess = false,
                    replyText = "",
                    spokenHinglish = "Mujhe koi response nahi mila dost!",
                    errorMessage = "Gemini returned an empty response. Please try rephrasing."
                )
            }

            // Extract a clean spoken summary (1-2 sentences) for TTS
            val spokenSummary = extractSpokenSummary(fullText)

            // Detect any device intent in user prompt
            val (detectedAction, target, payload, spokenTask) = detectDeviceAction(originalPrompt)

            GeminiResult(
                isSuccess = true,
                replyText = fullText,
                spokenHinglish = spokenSummary,
                spokenEnglishTask = spokenTask,
                detectedAction = detectedAction,
                actionTarget = target,
                actionPayload = payload
            )
        } catch (e: Exception) {
            Log.e("GeminiOnlineService", "Error parsing response JSON", e)
            GeminiResult(
                isSuccess = false,
                replyText = "",
                spokenHinglish = "Response samajh nahi aaya dost!",
                errorMessage = "Error parsing response from Gemini: ${e.message}"
            )
        }
    }

    private fun extractSpokenSummary(text: String): String {
        // Strip code blocks and markdown formatting for natural voice delivery
        val cleaned = text.lines()
            .filterNot { it.trim().startsWith("```") || it.trim().startsWith("#") }
            .joinToString(" ")
            .replace(Regex("[*`_#]"), "")
            .trim()

        val sentences = cleaned.split(Regex("[.!?\n]")).filter { it.isNotBlank() }
        val spoken = sentences.take(2).joinToString(". ").trim()
        return if (spoken.length > 180) spoken.take(180) + "..." else spoken.ifBlank { "Haan dost, maine dekh liya!" }
    }

    private fun detectDeviceAction(prompt: String): Tuple4<ActionKind, String, String, String?> {
        val lower = prompt.lowercase()
        return when {
            lower.contains("open youtube") || lower.contains("youtube chalao") ->
                Tuple4(ActionKind.OPEN_APP, "YOUTUBE", "", "Launching YouTube for you.")
            lower.contains("open camera") || lower.contains("camera kholo") ->
                Tuple4(ActionKind.OPEN_APP, "CAMERA", "", "Opening camera.")
            lower.contains("open settings") || lower.contains("settings kholo") ->
                Tuple4(ActionKind.OPEN_APP, "SETTINGS", "", "Opening settings.")
            lower.contains("open whatsapp") || lower.contains("whatsapp kholo") ->
                Tuple4(ActionKind.OPEN_APP, "WHATSAPP", "", "Opening WhatsApp.")
            lower.contains("take screenshot") || lower.contains("screenshot lo") ->
                Tuple4(ActionKind.SYSTEM_NAV, "SCREENSHOT", "", "Capturing screenshot.")
            lower.contains("go home") || lower == "home" ->
                Tuple4(ActionKind.SYSTEM_NAV, "HOME", "", "Navigating to home screen.")
            lower.contains("go back") || lower == "back" ->
                Tuple4(ActionKind.SYSTEM_NAV, "BACK", "", "Pressing back.")
            lower.startsWith("call ") || lower.contains("ko call karo") -> {
                val contact = prompt.split(" ").lastOrNull() ?: "Mom"
                Tuple4(ActionKind.CALL_PHONE, contact, "", "Dialing $contact.")
            }
            lower.startsWith("text ") || lower.contains("ko message karo") -> {
                val contact = prompt.split(" ").getOrNull(1) ?: "Mom"
                Tuple4(ActionKind.SEND_SMS, contact, prompt, "Opening message composer.")
            }
            else -> Tuple4(ActionKind.CHAT_ONLY, "", "", null)
        }
    }
}

data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
