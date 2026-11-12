package com.quicklock.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.quicklock.app.permissions.AccessibilityPermissionManager
import com.quicklock.app.shortcut.LockShortcutManager
import com.quicklock.app.ui.MainScreen
import com.quicklock.app.ui.QuickLockTheme

/** Status and setup screen. It never locks the screen itself. */
class MainActivity : ComponentActivity() {

    private val accessibilityEnabled = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LockShortcutManager.syncDynamicShortcut(this)
        val pinSupported = LockShortcutManager.isPinRequestSupported(this)

        setContent {
            QuickLockTheme {
                MainScreen(
                    accessibilityEnabled = accessibilityEnabled.value,
                    versionName = BuildConfig.VERSION_NAME,
                    pinShortcutSupported = pinSupported,
                    onOpenAccessibilitySettings = {
                        AccessibilityPermissionManager.openSettings(this)
                    },
                    onAddShortcut = ::requestShortcut,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accessibilityEnabled.value = AccessibilityPermissionManager.isServiceEnabled(this)
    }

    private fun requestShortcut() {
        if (!LockShortcutManager.requestPin(this)) {
            Toast.makeText(this, R.string.pin_not_supported, Toast.LENGTH_LONG).show()
        }
    }
}
