package com.quicklock.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent

/**
 * The only privileged component of QuickLock.
 *
 * Single responsibility: execute GLOBAL_ACTION_LOCK_SCREEN on request.
 * It subscribes to no accessibility events, cannot read window content and
 * cannot inject gestures.
 */
class LockAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        // Defensive: force "zero events" even if a platform applied a default
        // event mask while parsing accessibility_service_config.xml.
        serviceInfo?.let { info ->
            info.eventTypes = 0
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.flags = AccessibilityServiceInfo.DEFAULT
            info.notificationTimeout = 0
            setServiceInfo(info)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    companion object {
        /** Set by the framework while the service is connected, cleared on unbind. */
        @Volatile
        private var instance: LockAccessibilityService? = null

        /** True when the service is bound and can act right now. */
        val isConnected: Boolean
            get() = instance != null

        /**
         * Turns the screen off and locks the device.
         *
         * @return false when the service is not connected (permission revoked,
         *         process killed and not rebound yet) or on Android < 9, which has
         *         no lock-screen global action.
         */
        fun lockScreen(): Boolean {
            val service = instance ?: return false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
            return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
        }
    }
}
