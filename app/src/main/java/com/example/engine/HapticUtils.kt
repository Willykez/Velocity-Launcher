package com.example.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Deliberate, short haptic ticks for gesture recognition (swipe up/down, pinch,
 * double-tap) and folder/reorder interactions - the tactile "fluid, alive" feel
 * called out in the design doc. Kept to a single very short pulse everywhere so it
 * reads as an acknowledgement, never a buzz.
 *
 * All calls are wrapped in try/catch: haptics are a nice-to-have and must never be
 * able to crash the home screen (device without a vibrator, permission revoked by
 * the user post-install, etc).
 */
object HapticUtils {
    private const val TICK_MS = 12L
    private const val ERROR_MS = 40L

    private fun vibrator(context: Context): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    private fun pulse(context: Context, durationMs: Long) {
        try {
            val v = vibrator(context) ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Never let a missing/blocked vibrator take down the home screen.
        }
    }

    /** A gesture (swipe/pinch/double-tap/long-press) was recognized and is about to fire. */
    fun tick(context: Context) = pulse(context, TICK_MS)

    /** Something the user asked for didn't work (e.g. a wrong vault PIN). */
    fun error(context: Context) = pulse(context, ERROR_MS)
}
