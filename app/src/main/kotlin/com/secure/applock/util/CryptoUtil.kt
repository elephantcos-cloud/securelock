package com.secure.applock.util

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.SecureRandom
import android.util.Base64

object CryptoUtil {

    init { System.loadLibrary("securelock") }

    // Native C++ functions
    external fun nativeSecureCompare(a: String, b: String): Boolean
    external fun nativeGetAppSalt(): String

    private const val ITERATIONS  = 10_000
    private const val KEY_LENGTH   = 256
    private const val ALGORITHM    = "PBKDF2WithHmacSHA256"

    /** Hash a PIN/password with PBKDF2 + app salt + random salt */
    fun hashPassword(password: String): String {
        val randomSalt  = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val combinedSalt = (nativeGetAppSalt() + Base64.encodeToString(randomSalt, Base64.NO_WRAP)).toByteArray()
        val spec  = PBEKeySpec(password.toCharArray(), combinedSalt, ITERATIONS, KEY_LENGTH)
        val hash  = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        val saltB64 = Base64.encodeToString(randomSalt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        return "$saltB64:$hashB64"
    }

    /** Verify a password against a stored hash string */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts       = storedHash.split(":")
            if (parts.size != 2) return false
            val randomSalt  = Base64.decode(parts[0], Base64.NO_WRAP)
            val storedBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            val combinedSalt = (nativeGetAppSalt() + parts[0]).toByteArray()
            val spec        = PBEKeySpec(password.toCharArray(), combinedSalt, ITERATIONS, KEY_LENGTH)
            val computed    = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
            val computedB64 = Base64.encodeToString(computed, Base64.NO_WRAP)
            val storedB64   = Base64.encodeToString(storedBytes, Base64.NO_WRAP)
            nativeSecureCompare(computedB64, storedB64)
        } catch (e: Exception) { false }
    }
}
