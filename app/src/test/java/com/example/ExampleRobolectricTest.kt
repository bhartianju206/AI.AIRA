package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CryptoVault
import com.example.data.MemoryRepository
import com.example.data.db.AiraDatabase
import com.example.neural.EncryptedNeuralEngine
import com.example.neural.IntentType
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
    fun `verify neural engine intent planning offline`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AiraDatabase.getDatabase(context)
        val repo = MemoryRepository(db.airaDao())
        val engine = EncryptedNeuralEngine(repo)

        val plan = engine.analyzeAndPlan("Text Mom that I will be late")
        assertEquals(IntentType.SEND_MESSAGE, plan.intentType)
        assertTrue(plan.requiresBiometric)
        assertEquals(0L, plan.networkEgressBytes)
        assertTrue(plan.steps.isNotEmpty())
    }
}
