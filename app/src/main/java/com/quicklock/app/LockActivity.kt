package com.quicklock.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.quicklock.app.accessibility.LockAccessibilityService

/**
 * Invisible launcher target for the one-tap lock icon.
 *
 * It locks the device inside onCreate() and finishes immediately, so the user never
 * sees the app: the window is transparent (Theme.QuickLock.Lock) and the screen turns
 * off before any frame is drawn. If the accessibility service is not connected, the
 * activity explains why instead of failing silently.
 */
class LockActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LockAccessibilityService.lockScreen()) {
            finishAndRemoveTask()
            return
        }
        Toast.makeText(this, R.string.lock_unavailable, Toast.LENGTH_LONG).show()
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
        finish()
    }
}
