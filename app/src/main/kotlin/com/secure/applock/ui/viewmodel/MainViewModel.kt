package com.secure.applock.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.secure.applock.AppLockApplication
import com.secure.applock.data.db.LockedApp
import com.secure.applock.ui.theme.AppTheme
import com.secure.applock.util.CryptoUtil
import com.secure.applock.util.IconChanger
import com.secure.applock.util.UnlockState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val isLocked: Boolean
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db    = (app as AppLockApplication).database
    private val prefs = app.securePrefs
    private val dao   = db.lockedAppDao()

    // ── Prefs flows ──────────────────────────────────────────────────────────
    val isSetupDone  = prefs.isSetupDone.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val theme        = prefs.theme.map { runCatching { AppTheme.valueOf(it) }.getOrDefault(AppTheme.DARK) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.DARK)
    val lockEnabled  = prefs.lockEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val activeAlias  = prefs.activeAlias.stateIn(viewModelScope, SharingStarted.Eagerly, "ShieldAlias")

    // ── Locked apps ──────────────────────────────────────────────────────────
    val lockedApps = dao.getAllFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Installed apps ───────────────────────────────────────────────────────
    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps

    // ── Auth state ───────────────────────────────────────────────────────────
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _wrongPassword = MutableStateFlow(false)
    val wrongPassword: StateFlow<Boolean> = _wrongPassword

    // ─────────────────────────────────────────────────────────────────────────

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            val pm      = context.packageManager
            val locked  = dao.getAll().map { it.packageName }.toSet()
            val apps    = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .filter { it.packageName != context.packageName }
                .map { info ->
                    InstalledAppInfo(
                        packageName = info.packageName,
                        label       = pm.getApplicationLabel(info).toString(),
                        isLocked    = info.packageName in locked
                    )
                }
                .sortedBy { it.label }
            _installedApps.value = apps
        }
    }

    fun verifyPassword(input: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val stored = prefs.passwordHash.first()
            val ok     = stored != null && CryptoUtil.verifyPassword(input, stored)
            _wrongPassword.value = !ok
            if (ok) _isAuthenticated.value = true
            onResult(ok)
        }
    }

    fun setupPassword(password: String) {
        viewModelScope.launch {
            val hash = CryptoUtil.hashPassword(password)
            prefs.setPasswordHash(hash)
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val stored = prefs.passwordHash.first()
            val ok     = stored != null && CryptoUtil.verifyPassword(oldPassword, stored)
            if (ok) {
                prefs.setPasswordHash(CryptoUtil.hashPassword(newPassword))
            }
            onResult(ok)
        }
    }

    fun completeSetup() { viewModelScope.launch { prefs.setSetupDone(true) } }

    fun resetWrongPassword() { _wrongPassword.value = false }

    fun logout() { _isAuthenticated.value = false }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { prefs.setTheme(theme.name) }
    }

    fun setLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setLockEnabled(enabled)
            UnlockState.lockingEnabled = enabled
        }
    }

    fun toggleAppLock(app: InstalledAppInfo) {
        viewModelScope.launch {
            if (app.isLocked) dao.deleteByPackage(app.packageName)
            else dao.insert(LockedApp(app.packageName, app.label))
            _installedApps.update { list ->
                list.map { if (it.packageName == app.packageName) it.copy(isLocked = !it.isLocked) else it }
            }
        }
    }

    fun setActiveAlias(alias: String, context: Context) {
        viewModelScope.launch {
            prefs.setActiveAlias(alias)
            val info = IconChanger.aliases.firstOrNull { it.aliasShortName == alias }
            if (info != null) IconChanger.setAlias(context, info)
        }
    }
}
