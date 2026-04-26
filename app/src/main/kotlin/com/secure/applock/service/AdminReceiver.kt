package com.secure.applock.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class AdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) = Unit

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        "⚠ Disabling admin will remove SecureLock's uninstall protection. Enter your password in the app first."

    override fun onDisabled(context: Context, intent: Intent) = Unit
}
