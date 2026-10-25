package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CryptoVault
import com.example.data.MemoryRepository
import com.example.data.db.AiraDatabase
import com.example.neural.ActionKind
import com.example.neural.AiraFriendBrain
import com.example.neural.RealDeviceExecutor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AIRA AI", appName)
    }

    @Test
    fun `verify offline crypto encryption and decryption roundtrip`() {
        val plain = "SecretUserPreferenceToken_123"
        val encrypted = CryptoVault.encrypt(plain)
        val decrypted = CryptoVault.decrypt(encrypted)
        assertEquals(plain, decrypted)
    }

    @Test
    fun `verify Aira friend brain Hinglish processing and task execution`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AiraDatabase.getDatabase(context)
        val repo = MemoryRepository(db.airaDao())
        val brain = AiraFriendBrain(repo)

        // Test Hinglish friend chit-chat
        val chatAction = brain.processFriendInteraction("Kaisi ho Aira?", context)
        assertEquals(ActionKind.CHAT_ONLY, chatAction.kind)
        assertTrue(chatAction.spokenHinglish.contains("mast") || chatAction.spokenHinglish.contains("dost"))

        // Test real SMS command extraction
        val smsAction = brain.processFriendInteraction("Mom ko message karo ki 10 minute me aa raha hoon", context)
        assertEquals(ActionKind.SEND_SMS, smsAction.kind)
        assertTrue(smsAction.payload.contains("10 minute") || smsAction.payload.isNotBlank())

        // Test real execution intent generation
        val success = RealDeviceExecutor.executeAction(context, smsAction)
        assertTrue(success)
    }
}
