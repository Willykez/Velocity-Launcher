package com.example.engine

import java.security.MessageDigest

/**
 * The Hidden Apps vault PIN used to be stored and compared as plain text in Room
 * (and would have round-tripped in plain text through JSON backups had it not been
 * excluded there separately). This hashes it one-way before it's ever persisted, so
 * the raw PIN never touches disk.
 *
 * This is a lightweight, no-dependency safeguard appropriate for a local device PIN,
 * not a substitute for a real credential store - there is intentionally no need for
 * per-install salting here since the hash never leaves the device and is only ever
 * compared against a freshly-hashed user entry.
 */
object PinUtils {
    private const val SALT = "aura-launcher-vault-v1:"

    /** Returns a hex-encoded SHA-256 hash, or "" if [pin] is blank (PIN disabled). */
    fun hash(pin: String): String {
        if (pin.isBlank()) return ""
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((SALT + pin).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** True if [enteredPin] hashes to [storedHash], or if no PIN is configured at all. */
    fun matches(enteredPin: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return true
        return hash(enteredPin) == storedHash
    }
}
