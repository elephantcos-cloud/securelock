package com.secure.applock.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps")
data class LockedApp(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val addedAt: Long = System.currentTimeMillis()
)
