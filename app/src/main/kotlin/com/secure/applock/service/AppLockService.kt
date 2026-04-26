package com.secure.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.secure.applock.AppLockApplication
import com.secure.applock.LockActivity
import com.secure.applock.util.UnlockState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AppLockService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lockedPackages = setOf<String>()
    private var previousPackage = ""

    // Packages that should never be locked
    private val systemExempt = setOf(
        "com.android.launcher",
        "com.android.launcher2",
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.miui.home",
        "com.sec.android.app.launcher",
        "com.oppo.launcher",
        "com.realme.launcher",
        "com.android.systemui",
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        observeLockedApps()
    }

    private fun observeLockedApps() {
        scope.launch {
            val app = application as AppLockApplication
            app.database.lockedAppDao().getAllFlow().collect { list ->
                lockedPackages = list.map { it.packageName }.toSet()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return          // our own app — never lock
        if (pkg in systemExempt) return

        // When switching away from a locked app, re-lock it
        if (previousPackage != pkg && previousPackage.isNotEmpty()) {
            UnlockState.lock(previousPackage)
        }
        previousPackage = pkg

        if (!UnlockState.lockingEnabled) return
        if (pkg !in lockedPackages) return
        if (UnlockState.isUnlocked(pkg)) return

        // Intercept Settings app trying to show our App Info — detect via class name
        if (pkg == "com.android.settings" &&
            event.className?.toString()?.contains("AppInfoDashboard") == true) {
            showLockScreen(packageName) // lock settings access to OUR app info
            return
        }

        showLockScreen(pkg)
    }

    private fun showLockScreen(targetPackage: String) {
        val intent = Intent(this, LockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(LockActivity.EXTRA_PACKAGE, targetPackage)
        }
        startActivity(intent)
    }

    override fun onInterrupt() { scope.cancel() }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
