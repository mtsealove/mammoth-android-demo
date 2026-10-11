package com.example.mammothnative.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalMammothColors = staticCompositionLocalOf { mammothLightColors() }
val LocalMammothTypography = staticCompositionLocalOf { mammothTypography() }

/**
 * 디자인 토큰 접근자. `MammothTheme.colors`, `MammothTheme.typography` 로 사용한다.
 */
object MammothTheme {
    val colors: MammothColors
        @Composable @ReadOnlyComposable get() = LocalMammothColors.current

    val typography: MammothTypography
        @Composable @ReadOnlyComposable get() = LocalMammothTypography.current
}

/**
 * 앱 루트 테마. 웹 앱이 라이트 전용이므로 다크 테마/다이나믹 컬러는 쓰지 않는다.
 *
 * Material3 테마도 함께 깔아두어 기본 M3 컴포넌트(TopAppBar, TextField 등)가
 * Pretendard 와 맘모스 색상을 따라가게 한다.
 */
@Composable
fun MammothTheme(content: @Composable () -> Unit) {
    val colors = mammothLightColors()
    val typography = mammothTypography()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // 배경이 밝으므로 상태바/내비게이션바 아이콘을 어둡게.
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    CompositionLocalProvider(
        LocalMammothColors provides colors,
        LocalMammothTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = typography.toMaterialTypography(),
            shapes = androidx.compose.material3.Shapes(
                extraSmall = MammothShapes.sm,
                small = MammothShapes.sm,
                medium = MammothShapes.md,
                large = MammothShapes.lg,
                extraLarge = MammothShapes.xl,
            ),
            content = content,
        )
    }
}

private fun MammothColors.toMaterialColorScheme() = lightColorScheme(
    primary = primary,
    onPrimary = text.reverse,
    primaryContainer = primaryContainer,
    onPrimaryContainer = text.accent,
    secondary = surface.button.secondary,
    onSecondary = text.accent,
    background = surface.background.bg1,
    onBackground = text.primary,
    surface = surface.background.bg1,
    onSurface = text.primary,
    surfaceVariant = surface.background.bg2,
    onSurfaceVariant = text.secondary,
    outline = surface.border.default,
    outlineVariant = surface.border.default,
    error = error,
    onError = text.reverse,
    errorContainer = errorContainer,
    onErrorContainer = error,
)

private fun MammothTypography.toMaterialTypography() = Typography(
    headlineSmall = headline2,
    titleLarge = headline,
    titleMedium = title,
    titleSmall = body,
    bodyLarge = title,
    bodyMedium = body,
    bodySmall = label,
    labelLarge = body,
    labelMedium = label,
    labelSmall = caption,
)
