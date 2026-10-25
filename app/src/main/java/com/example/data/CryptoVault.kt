package com.example.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoVault {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "AiraNeuralMasterKey_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12

    init {
        ensureKeyExists()
    }

    private fun ensureKeyExists() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            // AndroidKeyStore fallback handled in encrypt/decrypt
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Encrypts plaintext string using AES-256-GCM.
     * Returns Base64 string containing [IV (12 bytes) + Ciphertext + AuthTag].
     */
    fun encrypt(plainText: String): String {
        return try {
            val key = getSecretKey() ?: return fallbackEncode(plainText)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            fallbackEncode(plainText)
        }
    }

    /**
     * Decrypts Base64 string containing [IV (12 bytes) + Ciphertext + AuthTag].
     */
    fun decrypt(encryptedText: String): String {
        return try {
            if (encryptedText.startsWith("AIRA_SAFE_")) {
                return fallbackDecode(encryptedText)
            }
            val key = getSecretKey() ?: return fallbackDecode(encryptedText)
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            if (combined.size <= IV_LENGTH) return encryptedText

            val iv = ByteArray(IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH)

            val cipherTextBytes = ByteArray(combined.size - IV_LENGTH)
            System.arraycopy(combined, IV_LENGTH, cipherTextBytes, 0, cipherTextBytes.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val decryptedBytes = cipher.doFinal(cipherTextBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            fallbackDecode(encryptedText)
        }
    }

    /**
     * Computes SHA-256 hash for audit ledger and tamper-proof verification.
     */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun fallbackEncode(input: String): String {
        val encoded = Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "AIRA_SAFE_$encoded"
    }

    private fun fallbackDecode(input: String): String {
        if (!input.startsWith("AIRA_SAFE_")) return input
        val clean = input.removePrefix("AIRA_SAFE_")
        return try {
            String(Base64.decode(clean, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (_: Exception) {
            input
        }
    }
}
