package com.secure.applock.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

data class AliasInfo(val displayName: String, val aliasShortName: String, val iconDescription: String)

object IconChanger {

    val aliases = listOf(
        AliasInfo("SecureLock",   "ShieldAlias", "Shield"),
        AliasInfo("Calculator",   "CalcAlias",   "Calculator"),
        AliasInfo("World Clock",  "ClockAlias",  "Clock"),
        AliasInfo("Quick Notes",  "NotesAlias",  "Notes"),
        AliasInfo("System Tools", "ToolsAlias",  "Tools"),
    )

    fun setAlias(context: Context, target: AliasInfo) {
        val pm  = context.packageManager
        val pkg = context.packageName
        aliases.forEach { alias ->
            val state = if (alias.aliasShortName == target.aliasShortName)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            runCatching {
                pm.setComponentEnabledSetting(
                    ComponentName(pkg, "$pkg.alias.${alias.aliasShortName}"),
                    state,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    fun currentAlias(context: Context): AliasInfo {
        val pm  = context.packageManager
        val pkg = context.packageName
        return aliases.firstOrNull { alias ->
            runCatching {
                pm.getComponentEnabledSetting(
                    ComponentName(pkg, "$pkg.alias.${alias.aliasShortName}")
                ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }.getOrDefault(false)
        } ?: aliases.first()
    }
}
