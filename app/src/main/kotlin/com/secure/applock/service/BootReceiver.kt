package com.secure.applock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_BOOT_COMPLETED) {
            // Accessibility service restarts automatically;
            // just ensure unlock state is cleared (it's in-memory anyway)
        }
    }
}
