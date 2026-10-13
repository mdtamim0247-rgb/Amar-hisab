package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldPrimary,
  onPrimary = Color.Black,
  primaryContainer = EmeraldDeep,
  onPrimaryContainer = EmeraldLight,
  secondary = GoldAccent,
  onSecondary = Color.Black,
  secondaryContainer = GoldDark,
  onSecondaryContainer = GoldLight,
  tertiary = SkyAccent,
  onTertiary = Color.Black,
  error = RoseExpense,
  onError = Color.White,
  background = DarkBackground,
  onBackground = TextPrimaryDark,
  surface = DarkSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = DarkSurfaceCard,
  onSurfaceVariant = TextSecondaryDark,
  outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldDark,
  onPrimary = Color.White,
  primaryContainer = EmeraldLight,
  onPrimaryContainer = EmeraldDeep,
  secondary = GoldDark,
  onSecondary = Color.White,
  secondaryContainer = GoldLight,
  onSecondaryContainer = Color.Black,
  tertiary = SkyAccent,
  onTertiary = Color.White,
  error = RoseDark,
  onError = Color.White,
  background = LightBackground,
  onBackground = TextPrimaryLight,
  surface = LightSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = LightSurfaceCard,
  onSurfaceVariant = TextSecondaryLight,
  outline = EmeraldDark.copy(alpha = 0.2f)
)

@Composable
fun AmarHisabTheme(
  darkTheme: Boolean = true, // Default to true matching the beautiful dark emerald mockup
  dynamicColor: Boolean = false, // Keep intentional Saudi-Bangladesh emerald theme
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    content = content
  )
}
