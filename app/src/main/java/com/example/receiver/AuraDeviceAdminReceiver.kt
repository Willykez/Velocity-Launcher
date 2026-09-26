package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Enables the "Lock Screen" gesture action (see [GestureAction.LOCK_SCREEN]).
 *
 * Android has no public, non-privileged API for a regular app to lock the screen -
 * the only sanctioned way is [android.app.admin.DevicePolicyManager.lockNow], which
 * requires the app to be an active device administrator with the FORCE_LOCK policy
 * (declared in res/xml/device_admin_receiver.xml). Activation is opt-in: the user must
 * explicitly grant it from Settings > App Vault & Privacy, Aura never requests it
 * automatically. If the user never enables it, the gesture simply falls back to
 * opening the Recents/Overview screen instead (see AppLoader.lockScreenOrRecents).
 *
 * This receiver intentionally implements none of the other DeviceAdminReceiver
 * callbacks (no wipe, no password enforcement) - force-lock is the only policy
 * requested in the XML descriptor, so that's the only capability Android will grant.
 */
class AuraDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}
