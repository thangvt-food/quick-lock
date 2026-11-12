package com.quicklock.app.permissions

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
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
     * Opens the accessibility page for QuickLock, falling back to the generic
     * accessibility settings screen on ROMs that ignore the details intent.
     */
    fun openSettings(context: Context) {
        val details = Intent(Settings.ACTION_ACCESSIBILITY_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            putExtra(Intent.EXTRA_COMPONENT_NAME, serviceComponent(context))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fallback = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(details)
        } catch (_: RuntimeException) {
            // OEM settings apps may not export the details screen.
            context.startActivity(fallback)
        }
    }
}
