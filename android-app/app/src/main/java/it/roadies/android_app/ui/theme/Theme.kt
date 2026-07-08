package it.roadies.android_app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val successText: Color,
    val warning: Color,
    val warningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val star: Color,
    val header: Color,
    val onHeader: Color,
    val neutralBackground: Color,
    val chatBubbleMine: Color,
)

private val LightExtendedColors = ExtendedColors(
    success = SuccessLight,
    successContainer = SuccessContainerLight,
    successText = SuccessTextLight,
    warning = WarningLight,
    warningContainer = WarningContainerLight,
    danger = DangerLight,
    dangerContainer = DangerContainerLight,
    star = StarGold,
    header = BrandHeaderLight,
    onHeader = OnBrandHeader,
    neutralBackground = NeutralBackgroundLight,
    chatBubbleMine = ChatBubbleMineLight,
)

private val DarkExtendedColors = ExtendedColors(
    success = SuccessDark,
    successContainer = SuccessContainerDark,
    successText = SuccessTextDark,
    warning = WarningDark,
    warningContainer = WarningContainerDark,
    danger = DangerDark,
    dangerContainer = DangerContainerDark,
    star = StarGold,
    header = BrandHeaderDark,
    onHeader = OnBrandHeader,
    neutralBackground = NeutralBackgroundDark,
    chatBubbleMine = ChatBubbleMineDark,
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/** Extended semantic colors (success/warning/danger/star/brand chrome) matching the current theme. */
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

private val DarkColorScheme = darkColorScheme(
    primary                  = BluePrimary80,
    onPrimary                = OnBluePrimaryDark,
    primaryContainer         = BluePrimaryContainerDark,
    onPrimaryContainer       = OnBluePrimaryContainerDark,
    secondary                = BlueSecondary80,
    onSecondary              = OnBlueSecondaryDark,
    secondaryContainer       = BlueSecondaryContainerDark,
    onSecondaryContainer     = OnBlueSecondaryContainerDark,
    tertiary                 = Orange80,
    onTertiary               = OnOrangeDark,
    tertiaryContainer        = OrangeContainerDark,
    onTertiaryContainer      = OnOrangeContainerDark,
    background               = SurfaceDark,
    onBackground             = OnSurfaceDark,
    surface                  = SurfaceDark,
    onSurface                = OnSurfaceDark,
    surfaceVariant           = SurfaceVariantDark,
    onSurfaceVariant         = OnSurfaceVariantDark,
    outline                  = OutlineDark,
    outlineVariant           = OutlineVariantDark,
    error                    = ErrorDark,
    onError                  = OnErrorDark,
    errorContainer           = ErrorContainerDark,
    onErrorContainer         = OnErrorContainerDark,
)

private val LightColorScheme = lightColorScheme(
    primary                  = BluePrimary40,
    onPrimary                = OnBluePrimaryLight,
    primaryContainer         = BluePrimaryContainerLight,
    onPrimaryContainer       = OnBluePrimaryContainerLight,
    secondary                = BlueSecondary40,
    onSecondary              = OnBlueSecondaryLight,
    secondaryContainer       = BlueSecondaryContainerLight,
    onSecondaryContainer     = OnBlueSecondaryContainerLight,
    tertiary                 = Orange40,
    onTertiary               = OnOrangeLight,
    tertiaryContainer        = OrangeContainerLight,
    onTertiaryContainer      = OnOrangeContainerLight,
    background               = SurfaceLight,
    onBackground             = OnSurfaceLight,
    surface                  = SurfaceLight,
    onSurface                = OnSurfaceLight,
    surfaceVariant           = SurfaceVariantLight,
    onSurfaceVariant         = OnSurfaceVariantLight,
    outline                  = OutlineLight,
    outlineVariant           = OutlineVariantLight,
    error                    = ErrorLight,
    onError                  = OnErrorLight,
    errorContainer           = ErrorContainerLight,
    onErrorContainer         = OnErrorContainerLight,
)

@Composable
fun AndroidappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color would override our brand palette with wallpaper-derived tones,
    // so it defaults off to keep the app's identity consistent across devices.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
