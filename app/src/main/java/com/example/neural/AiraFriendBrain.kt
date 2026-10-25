package com.example.neural

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.accessibility.AiraAccessibilityService
import com.example.data.MemoryRepository

enum class ActionKind {
    SEND_SMS,
    CALL_PHONE,
    OPEN_APP,
    READ_SCREEN,
    WEB_SEARCH,
    SYSTEM_NAV,
    SET_TIMER,
    CHAT_ONLY
}

data class AiraExecutionAction(
    val kind: ActionKind,
    val target: String = "",
    val payload: String = "",
    val spokenHinglish: String,
    val textReplyHinglish: String
)

class AiraFriendBrain(private val repository: MemoryRepository) {

    /**
     * Processes input from user as a natural friend conversation in Hinglish,
     * determining whether a real phone task should be executed or natural friendly chat.
     */
    suspend fun processFriendInteraction(input: String, context: Context): AiraExecutionAction {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Sending SMS / Messaging
        if (lower.startsWith("text ") || lower.startsWith("send message") || lower.startsWith("message ") ||
            lower.contains("ko message") || lower.contains("ko text") || lower.contains("bhejo") || lower.contains("bhej do")
        ) {
            val (recipient, message) = extractContactAndMessage(trimmed)
            val resolvedRecipient = if (recipient.equals("Mom", ignoreCase = true) || recipient.equals("Mummy", ignoreCase = true)) {
                repository.getDecryptedValue("contact_mom") ?: "Mom"
            } else {
                recipient
            }

            return AiraExecutionAction(
                kind = ActionKind.SEND_SMS,
                target = resolvedRecipient,
                payload = message,
                spokenHinglish = "Haan bilkul dost! Main abhi $resolvedRecipient ke liye message khol rahi hoon: '$message'.",
                textReplyHinglish = "Haan dost! Main abhi $resolvedRecipient ko message bhejne ke liye SMS client khol rahi hoon. Message: \"$message\""
            )
        }

        // 2. Calling / Dialing phone
        if (lower.startsWith("call ") || lower.startsWith("dial ") || lower.contains("ko phone") || lower.contains("ko call")) {
            val contact = extractContactForCall(trimmed)
            return AiraExecutionAction(
                kind = ActionKind.CALL_PHONE,
                target = contact,
                spokenHinglish = "Haanji, abhi $contact ko call mila rahi hoon!",
                textReplyHinglish = "Abhi $contact ke liye dialer khol rahi hoon, call connect ho raha hai dost!"
            )
        }

        // 3. Reading / Summarizing screen
        if (lower.contains("read screen") || lower.contains("screen padh") || lower.contains("screen dekho") ||
            lower.contains("what is on my screen") || lower.contains("kya likha hai") || lower.contains("summarize screen")
        ) {
            val service = AiraAccessibilityService.instance.value
            return if (service != null && AiraAccessibilityService.isConnected.value) {
                val snapshot = service.captureActiveScreen()
                val summary = if (snapshot.readableTexts.isNotEmpty()) {
                    "Main screen dekh rahi hoon dost! Isme likha hai: " + snapshot.readableTexts.take(4).joinToString(", ")
                } else {
                    "Screen par active window dikh raha hai dost, par abhi koi readable text nahi mila."
                }
                AiraExecutionAction(
                    kind = ActionKind.READ_SCREEN,
                    target = "ACTIVE_WINDOW",
                    payload = summary,
                    spokenHinglish = summary,
                    textReplyHinglish = summary
                )
            } else {
                AiraExecutionAction(
                    kind = ActionKind.READ_SCREEN,
                    target = "ACCESSIBILITY_SETTINGS",
                    spokenHinglish = "Arre dost, mujhe tumhari screen dekhne ke liye Accessibility permission chahiye hogi! Chalo main settings khol deti hoon, AIRA ko ON kar do na!",
                    textReplyHinglish = "Arre dost, mujhe tumhari screen dekhne ke liye Accessibility permission chahiye hogi! Main settings khol rahi hoon, AIRA AI ko ON kar do please!"
                )
            }
        }

        // 4. Open Settings
        if (lower.contains("open settings") || lower.contains("settings kholo") || lower.contains("setting khol do")) {
            return AiraExecutionAction(
                kind = ActionKind.OPEN_APP,
                target = "SETTINGS",
                spokenHinglish = "Ji dost! Main device settings khol rahi hoon.",
                textReplyHinglish = "Lo dost, maine Settings open kar di hai!"
            )
        }

        // 5. Open YouTube
        if (lower.contains("youtube") || lower.contains("gaana chalao") || lower.contains("video chalao")) {
            return AiraExecutionAction(
                kind = ActionKind.OPEN_APP,
                target = "YOUTUBE",
                spokenHinglish = "Arre waah, YouTube chala rahi hoon! Mast video dekho aur chill karo.",
                textReplyHinglish = "YouTube launch kar rahi hoon dost, enjoy karo!"
            )
        }

        // 6. Open Camera / Take Photo
        if (lower.contains("camera") || lower.contains("photo khincho") || lower.contains("selfie")) {
            return AiraExecutionAction(
                kind = ActionKind.OPEN_APP,
                target = "CAMERA",
                spokenHinglish = "Camera open kar rahi hoon dost! Smile karo!",
                textReplyHinglish = "Camera ready hai dost, mast photo lo!"
            )
        }

        // 7. Open WhatsApp
        if (lower.contains("whatsapp")) {
            return AiraExecutionAction(
                kind = ActionKind.OPEN_APP,
                target = "WHATSAPP",
                spokenHinglish = "WhatsApp khol rahi hoon dost!",
                textReplyHinglish = "WhatsApp open kar diya hai dost!"
            )
        }

        // 8. Search Web / Google
        if (lower.startsWith("search ") || lower.startsWith("google ") || lower.contains("search karo")) {
            val query = trimmed.removePrefix("search ").removePrefix("Search ").removePrefix("google ").removePrefix("Google ").trim()
            return AiraExecutionAction(
                kind = ActionKind.WEB_SEARCH,
                target = query,
                payload = query,
                spokenHinglish = "Main Google pe '$query' search kar rahi hoon dost!",
                textReplyHinglish = "Web search open kar rahi hoon: \"$query\""
            )
        }

        // 9. Navigation: Home, Back, Recents, Screenshot
        if (lower.contains("go home") || lower == "home" || lower.contains("home screen") || lower.contains("home jao")) {
            return AiraExecutionAction(
                kind = ActionKind.SYSTEM_NAV,
                target = "HOME",
                spokenHinglish = "Home screen par le chal rahi hoon dost!",
                textReplyHinglish = "Home screen par navigate kar diya dost!"
            )
        }

        if (lower.contains("go back") || lower == "back" || lower.contains("peeche jao")) {
            return AiraExecutionAction(
                kind = ActionKind.SYSTEM_NAV,
                target = "BACK",
                spokenHinglish = "Back kar diya dost!",
                textReplyHinglish = "Back button press kar diya dost!"
            )
        }

        if (lower.contains("recents") || lower.contains("recent apps") || lower.contains("apps dikhao")) {
            return AiraExecutionAction(
                kind = ActionKind.SYSTEM_NAV,
                target = "RECENTS",
                spokenHinglish = "Recent apps khol rahi hoon dost!",
                textReplyHinglish = "Recent apps screen khol diya hai dost!"
            )
        }

        if (lower.contains("screenshot")) {
            return AiraExecutionAction(
                kind = ActionKind.SYSTEM_NAV,
                target = "SCREENSHOT",
                spokenHinglish = "Screenshot le rahi hoon dost!",
                textReplyHinglish = "Screenshot capture trigger kar diya dost!"
            )
        }

        // 10. Timer / Alarm
        if (lower.contains("timer") || lower.contains("alarm")) {
            val seconds = extractTimerSeconds(trimmed)
            return AiraExecutionAction(
                kind = ActionKind.SET_TIMER,
                target = "$seconds",
                payload = "${seconds / 60} minute",
                spokenHinglish = "Haan dost, main timer set kar rahi hoon!",
                textReplyHinglish = "Timer app me time set kar diya hai dost!"
            )
        }

        // 11. Conversational Friend Chit-Chat (Naturally like a real girl best friend in Hinglish)
        val chitChatReply = generateFriendChitChat(trimmed)
        return AiraExecutionAction(
            kind = ActionKind.CHAT_ONLY,
            spokenHinglish = chitChatReply,
            textReplyHinglish = chitChatReply
        )
    }

    private fun generateFriendChitChat(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("kya haal hai") || lower.contains("kaisi ho") || lower.contains("how are you") -> {
                "Main ekdum mast hoon dost! Tum batao, tumhara din kaisa chal raha hai? Kuch naya hua kya aaj? 😉"
            }
            lower.contains("bore") || lower.contains("boring") -> {
                "Arre yaar aise kaise bore ho rahe ho jab main tumhare saath hoon! Chalo batao, YouTube pe koi comedy video dekhein ya koi gaana sunein?"
            }
            lower.contains("love you") || lower.contains("you are sweet") || lower.contains("pyari ho") || lower.contains("acchi ho") -> {
                "Aww thank you so much! Tum bhi mere best dost ho. Tumhare saath rehna mujhe sach me bohot accha lagta hai! ❤️"
            }
            lower.contains("who are you") || lower.contains("kaun ho tum") || lower.contains("tum kaun ho") || lower.contains("naam kya hai") -> {
                "Arre bhool gaye kya? Main Aira hoon, tumhari close friend! Hamesha tumhare saath rehti hoon aur jo bhi phone me help chahiye ho turant kar deti hoon."
            }
            lower.contains("kya kar rahi ho") || lower.contains("kya kar rahi") || lower.contains("what are you doing") -> {
                "Bas yahin tumhare saath baith kar baatein kar rahi hoon! Batao na, abhi kya chal raha hai tumhare mind me?"
            }
            lower.contains("good morning") || lower.contains("subah ho gayi") -> {
                "Good Morning dost! Uth gaye tum? Chalo aaj ka din ekdum mast plan karte hain. Chai ya coffee pee li tumne?"
            }
            lower.contains("good night") || lower.contains("so raha hoon") || lower.contains("neend aa rahi") -> {
                "Good Night dost! Phone side me rakho aur aaraam se so jao. Sweet dreams, kal subah fresh start karenge!"
            }
            lower.contains("thank") || lower.contains("shukriya") || lower.contains("dhanyawad") -> {
                "Dosti me no sorry, no thank you! Hamesha yaad rakhna, main tumhare liye hamesha yahin hoon."
            }
            lower.contains("sad hoon") || lower.contains("mood off") || lower.contains("tension") || lower.contains("pareshan") -> {
                "Arre kya hua dost? Tension mat lo please, sab theek ho jayega. Main hoon na tumhare saath! Chalo deep breath lo aur smile karo!"
            }
            lower.contains("joke") || lower.contains("chutkula") || lower.contains("kuch sunao") -> {
                "Ek mast baat suno: Teacher ne pucha - 'Agar 10 me se 2 gaye toh kya bacha?' Bachha bola - 'Sir, recharge khatam!' Haha, kaisa laga?"
            }
            else -> {
                "Haan dost, bolo na! Main sun rahi hoon. Chaho toh kisi ko text bhejwa lo, screen padhwa lo, ya bas aaram se baatein karo!"
            }
        }
    }

    private fun extractContactAndMessage(input: String): Pair<String, String> {
        val clean = input.trim()
        val lower = clean.lowercase()

        // Pattern 1: "Mom ko message karo ki..."
        if (lower.contains(" ko message") || lower.contains(" ko text")) {
            val koIdx = lower.indexOf(" ko")
            val contact = if (koIdx > 0) clean.substring(0, koIdx).trim() else "Mom"
            val msgIdx = lower.indexOf("message")
            val after = if (msgIdx > 0 && msgIdx + 7 < clean.length) clean.substring(msgIdx + 7).trim() else ""
            val message = after.removePrefix("karo ki ").removePrefix("bhejo ki ").removePrefix("ki ").removePrefix("karo ").trim()
            return Pair(contact.ifBlank { "Mom" }, message.ifBlank { "Hello!" })
        }

        // Pattern 2: "Text Mom that I am on my way"
        var textPart = clean
        if (lower.startsWith("text ")) textPart = clean.substring(5).trim()
        else if (lower.startsWith("send message to ")) textPart = clean.substring(16).trim()
        else if (lower.startsWith("message ")) textPart = clean.substring(8).trim()

        val delimiters = listOf(" that ", " saying ", " : ", ": ", " with text ", " - ", " ")
        for (del in delimiters) {
            if (textPart.contains(del, ignoreCase = true)) {
                val idx = textPart.indexOf(del, ignoreCase = true)
                val c = textPart.substring(0, idx).trim()
                val m = textPart.substring(idx + del.length).trim()
                if (c.isNotBlank() && m.isNotBlank()) {
                    return Pair(c, m)
                }
            }
        }

        val parts = textPart.split(" ", limit = 2)
        return Pair(parts.firstOrNull()?.trim() ?: "Mom", parts.getOrNull(1)?.trim() ?: "Hello from Aira!")
    }

    private fun extractContactForCall(input: String): String {
        val clean = input.trim()
        val lower = clean.lowercase()
        val koIdx = lower.indexOf(" ko")
        if (koIdx > 0) {
            return clean.substring(0, koIdx).trim()
        }
        val parts = clean.split(" ")
        return parts.getOrNull(1) ?: "Mom"
    }

    private fun extractTimerSeconds(input: String): Int {
        val digits = Regex("\\d+").find(input)?.value?.toIntOrNull() ?: 5
        return if (input.contains("second", ignoreCase = true)) digits else digits * 60
    }
}
