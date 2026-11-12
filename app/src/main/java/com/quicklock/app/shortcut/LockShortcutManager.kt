package com.quicklock.app.shortcut

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import com.quicklock.app.LockActivity
import com.quicklock.app.R

/**
 * Launcher shortcuts that point straight at [LockActivity].
 *
 * The Dock icon itself comes from the "LockShortcutAlias" activity-alias in the
 * manifest: it is a real launcher icon, so every launcher (HyperOS included) can
 * place it on the Home screen or in the Dock and keeps it across reboots/updates.
 * The ShortcutManager entries below are an additional convenience: a pinnable
 * shortcut and a long-press entry on the app icon.
 */
object LockShortcutManager {

    private const val SHORTCUT_ID = "quicklock_lock"

    fun isPinRequestSupported(context: Context): Boolean =
        context.getSystemService(ShortcutManager::class.java)
            ?.isRequestPinShortcutSupported == true

    /** Asks the launcher to pin the one-tap lock shortcut. */
    fun requestPin(context: Context): Boolean {
        val manager = context.getSystemService(ShortcutManager::class.java) ?: return false
        if (!manager.isPinRequestSupported) return false
        return manager.requestPinShortcut(buildShortcut(context), null)
    }

    /** Adds "Lock the screen" to the long-press menu of the app icon. */
    fun syncDynamicShortcut(context: Context) {
        val manager = context.getSystemService(ShortcutManager::class.java) ?: return
        if (manager.maxShortcutCountPerActivity <= 0) return
        if (manager.dynamicShortcuts.any { it.id == SHORTCUT_ID }) return
        manager.addDynamicShortcuts(listOf(buildShortcut(context)))
    }

    private fun buildShortcut(context: Context): ShortcutInfo =
        ShortcutInfo.Builder(context, SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_name))
            .setLongLabel(context.getString(R.string.shortcut_long_label))
            .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_lock))
            .setActivity(ComponentName(context, LockActivity::class.java))
            .setIntent(
                Intent(context, LockActivity::class.java)
                    .setAction(Intent.ACTION_MAIN),
            )
            .build()
}
