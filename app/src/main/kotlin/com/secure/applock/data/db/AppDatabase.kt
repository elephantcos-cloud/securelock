package com.secure.applock.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LockedApp::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lockedAppDao(): LockedAppDao
}
