package com.secure.applock.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("secure_prefs")

class SecurePrefs(private val ctx: Context) {

    companion object {
        val KEY_PASSWORD_HASH = stringPreferencesKey("pwd_hash")
        val KEY_SETUP_DONE    = booleanPreferencesKey("setup_done")
        val KEY_THEME         = stringPreferencesKey("theme")
        val KEY_ACTIVE_ALIAS  = stringPreferencesKey("active_alias")
        val KEY_LOCK_ENABLED  = booleanPreferencesKey("lock_enabled")
    }

    val passwordHash: Flow<String?> = ctx.dataStore.data.map { it[KEY_PASSWORD_HASH] }
    val isSetupDone:  Flow<Boolean> = ctx.dataStore.data.map { it[KEY_SETUP_DONE] ?: false }
    val theme:        Flow<String>  = ctx.dataStore.data.map { it[KEY_THEME]        ?: "DARK" }
    val activeAlias:  Flow<String>  = ctx.dataStore.data.map { it[KEY_ACTIVE_ALIAS] ?: "ShieldAlias" }
    val lockEnabled:  Flow<Boolean> = ctx.dataStore.data.map { it[KEY_LOCK_ENABLED]  ?: true }

    suspend fun setPasswordHash(hash: String) =
        ctx.dataStore.edit { it[KEY_PASSWORD_HASH] = hash }

    suspend fun setSetupDone(done: Boolean) =
        ctx.dataStore.edit { it[KEY_SETUP_DONE] = done }

    suspend fun setTheme(theme: String) =
        ctx.dataStore.edit { it[KEY_THEME] = theme }

    suspend fun setActiveAlias(alias: String) =
        ctx.dataStore.edit { it[KEY_ACTIVE_ALIAS] = alias }

    suspend fun setLockEnabled(enabled: Boolean) =
        ctx.dataStore.edit { it[KEY_LOCK_ENABLED] = enabled }
}
