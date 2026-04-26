package com.secure.applock.util

/**
 * In-memory state tracking which apps have been unlocked this session.
 * Cleared when the user leaves the unlocked app.
 */
object UnlockState {
    private val unlockedPackages = mutableSetOf<String>()
    var lockingEnabled: Boolean = true

    fun unlock(packageName: String) { unlockedPackages.add(packageName) }
    fun lock(packageName: String)   { unlockedPackages.remove(packageName) }
    fun lockAll()                   { unlockedPackages.clear() }
    fun isUnlocked(packageName: String) = packageName in unlockedPackages
}
