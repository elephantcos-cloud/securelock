package com.secure.applock

import android.app.Application
import androidx.room.Room
import com.secure.applock.data.db.AppDatabase
import com.secure.applock.data.prefs.SecurePrefs

class AppLockApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "secure_lock.db").build()
    }
    val securePrefs: SecurePrefs by lazy { SecurePrefs(this) }
}
