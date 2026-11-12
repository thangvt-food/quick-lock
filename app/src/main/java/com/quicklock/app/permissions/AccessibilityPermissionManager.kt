package com.quicklock.app.permissions

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.quicklock.app.accessibility.LockAccessibilityService

/**
 * Reads and navigates to the Accessibility permission.
 *
 * The enabled state is queried through AccessibilityManager (the public, supported
 * API) instead of parsing Settings.Secure strings.
 */
object AccessibilityPermissionManager {

    fun serviceComponent(context: Context): ComponentName =
        ComponentName(context, LockAccessibilityService::class.java)

    fun isServiceEnabled(context: Context): Boolean {
        val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
        val target = serviceComponent(context)
        val fullId = target.flattenToString()
        val shortId = target.flattenToShortString()
        return manager
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { info -> info.id == fullId || info.id == shortId }
    }

    /**
     * Opens the system accessibility screen, where QuickLock is listed under
     * "Downloaded apps". Falls back to the root Settings screen on ROMs that do not
     * export the accessibility page.
     */
    fun openSettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: ActivityNotFoundException) {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
