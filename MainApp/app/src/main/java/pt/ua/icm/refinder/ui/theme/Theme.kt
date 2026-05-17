package pt.ua.icm.refinder.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RefinderPrimary,
    secondary = RefinderSecondary,

    background = RefinderBackground,
    surface = RefinderSurface,

    onPrimary = RefinderBackground,
    onSecondary = RefinderTextPrimary,

    onBackground = RefinderTextPrimary,
    onSurface = RefinderTextPrimary
)

@Composable
fun ReFinderTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = RefinderBackground.toArgb()
            window.navigationBarColor = RefinderBackground.toArgb()

            WindowCompat.getInsetsController(
                window,
                view
            ).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}