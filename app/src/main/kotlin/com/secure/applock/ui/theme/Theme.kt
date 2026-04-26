package com.secure.applock.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String) {
    DARK("Dark"),
    AMOLED("AMOLED Black"),
    OCEAN("Ocean Blue"),
    SUNSET("Sunset Red"),
    FOREST("Forest Green"),
    LIGHT("Light")
}

private fun darkScheme(
    bg: Color, surface: Color, surfaceVar: Color,
    primary: Color, secondary: Color,
    onBg: Color = Color(0xFFE8E8F5), onSurface: Color = Color(0xFFCCCCE0),
    outline: Color = Color(0xFF44446A)
) = darkColorScheme(
    background          = bg,
    surface             = surface,
    surfaceVariant      = surfaceVar,
    primary             = primary,
    onPrimary           = Color.White,
    secondary           = secondary,
    onSecondary         = Color.Black,
    onBackground        = onBg,
    onSurface           = onSurface,
    error               = Color(0xFFCF6679),
    outline             = outline,
    surfaceContainer        = surfaceVar,
    surfaceContainerHigh    = surfaceVar,
    surfaceContainerHighest = surfaceVar,
)

@Composable
fun SecureLockTheme(appTheme: AppTheme = AppTheme.DARK, content: @Composable () -> Unit) {
    val colorScheme = when (appTheme) {
        AppTheme.DARK   -> darkScheme(DarkBg, DarkSurface, DarkSurfaceVariant, DarkPrimary, DarkSecondary, outline = DarkOutline)
        AppTheme.AMOLED -> darkScheme(AmoledBg, AmoledSurface, AmoledSurfaceVariant, AmoledPrimary, AmoledSecondary)
        AppTheme.OCEAN  -> darkScheme(OceanBg, OceanSurface, OceanSurfaceVariant, OceanPrimary, OceanSecondary)
        AppTheme.SUNSET -> darkScheme(SunsetBg, SunsetSurface, SunsetSurfaceVariant, SunsetPrimary, SunsetSecondary)
        AppTheme.FOREST -> darkScheme(ForestBg, ForestSurface, ForestSurfaceVariant, ForestPrimary, ForestSecondary)
        AppTheme.LIGHT  -> lightColorScheme(
            background        = LightBg,
            surface           = LightSurface,
            surfaceVariant    = LightSurfaceVariant,
            primary           = LightPrimary,
            onPrimary         = Color.White,
            secondary         = LightSecondary,
            onBackground      = LightOnBg,
            onSurface         = LightOnSurface,
            outline           = LightOutline,
            surfaceContainer        = LightSurfaceVariant,
            surfaceContainerHigh    = LightSurfaceVariant,
            surfaceContainerHighest = LightSurfaceVariant,
        )
    }
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
