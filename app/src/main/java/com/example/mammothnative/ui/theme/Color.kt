package com.example.mammothnative.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 웹(mammoth-renwal-client) `src/constants/colors.ts` 의 원시 팔레트.
 * 화면 코드에서는 직접 쓰지 말고 [MammothColors] 의 의미(semantic) 토큰을 사용한다.
 */
object MammothPalette {
    val Gray0 = Color(0xFFFFFFFF)
    val Gray1 = Color(0xFFFCFAF8)
    val Gray2 = Color(0xFFF3F0ED)
    val Gray3 = Color(0xFFE4E1DE)
    val Gray4 = Color(0xFFD1CDC9)
    val Gray5 = Color(0xFFAFA8A5)
    val Gray6 = Color(0xFF6E6864)
    val Gray7 = Color(0xFF504A46)
    val Gray8 = Color(0xFF262320)
    val Gray9 = Color(0xFF110F0D)

    val Primary1 = Color(0xFF76584C)
    val Primary2 = Color(0xFFA49D9A)
    val Primary3 = Color(0xFFF3EFE7)

    val Secondary1 = Color(0xFFF7F1EB)
    val Secondary2 = Color(0xFFFCFAF8)

    val Red = Color(0xFFAF2121)
    val Green = Color(0xFF2CA842)
    val Blue = Color(0xFF6096FF)

    val RedOpacity8 = Color(0xFFFFE8E8)
    val GreenOpacity8 = Color(0xFFE0F4E1)
    val BlueOpacity8 = Color(0xFFE6EFFF)

    /** 토스트 배경 (rgba(80, 74, 70, 0.75)) */
    val Dim75 = Color(0xBF504A46)
}

@Immutable
data class MammothTextColors(
    val primary: Color,
    val secondary: Color,
    val reverse: Color,
    val disabled: Color,
    val unselected: Color,
    val accent: Color,
    val error: Color,
    val link: Color,
    val success: Color,
)

@Immutable
data class MammothIconColors(
    val default: Color,
    val accent1: Color,
    val accent2: Color,
    val disabled: Color,
    val unselected: Color,
    val reverse: Color,
    val error: Color,
    val link: Color,
    val success: Color,
)

@Immutable
data class MammothButtonColors(
    val default: Color,
    val secondary: Color,
    val unselected: Color,
    val disabled: Color,
)

@Immutable
data class MammothBorderColors(
    val default: Color,
    val accent: Color,
    val accent3: Color,
    val disabled: Color,
    val error: Color,
)

@Immutable
data class MammothBackgroundColors(
    /** 기본 화이트 배경 */
    val bg1: Color,
    /** 베이지 톤 섹션 배경 */
    val bg2: Color,
    /** 아주 옅은 그레이 배경 */
    val bg3: Color,
    val disabled: Color,
)

@Immutable
data class MammothSurfaceColors(
    val button: MammothButtonColors,
    val border: MammothBorderColors,
    val background: MammothBackgroundColors,
)

/**
 * 앱 전체에서 사용하는 의미 기반 색상 토큰.
 * `MammothTheme.colors.text.primary` 처럼 접근한다.
 */
@Immutable
data class MammothColors(
    val text: MammothTextColors,
    val icon: MammothIconColors,
    val surface: MammothSurfaceColors,
    val primary: Color,
    val primaryContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val success: Color,
    val successContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val dim: Color,
)

/**
 * 웹 앱이 라이트 테마 전용이라 색상 세트도 하나만 둔다.
 * 추후 다크 테마가 필요하면 동일한 구조의 세트를 추가해 [MammothTheme] 에서 분기한다.
 */
fun mammothLightColors(): MammothColors = MammothColors(
    text = MammothTextColors(
        primary = MammothPalette.Gray9,
        secondary = MammothPalette.Gray7,
        reverse = MammothPalette.Gray1,
        disabled = MammothPalette.Gray5,
        unselected = MammothPalette.Gray5,
        accent = MammothPalette.Primary1,
        error = MammothPalette.Red,
        link = MammothPalette.Blue,
        success = MammothPalette.Green,
    ),
    icon = MammothIconColors(
        default = MammothPalette.Gray7,
        accent1 = MammothPalette.Primary1,
        accent2 = MammothPalette.Primary2,
        disabled = MammothPalette.Gray3,
        unselected = MammothPalette.Gray5,
        reverse = MammothPalette.Gray1,
        error = MammothPalette.Red,
        link = MammothPalette.Blue,
        success = MammothPalette.Green,
    ),
    surface = MammothSurfaceColors(
        button = MammothButtonColors(
            default = MammothPalette.Primary1,
            secondary = MammothPalette.Secondary1,
            unselected = MammothPalette.Gray1,
            disabled = MammothPalette.Gray3,
        ),
        border = MammothBorderColors(
            default = MammothPalette.Gray3,
            accent = MammothPalette.Primary1,
            accent3 = MammothPalette.Primary3,
            disabled = MammothPalette.Gray3,
            error = MammothPalette.Red,
        ),
        background = MammothBackgroundColors(
            bg1 = MammothPalette.Gray0,
            bg2 = MammothPalette.Secondary1,
            bg3 = MammothPalette.Secondary2,
            disabled = MammothPalette.Gray2,
        ),
    ),
    primary = MammothPalette.Primary1,
    primaryContainer = MammothPalette.Primary3,
    error = MammothPalette.Red,
    errorContainer = MammothPalette.RedOpacity8,
    success = MammothPalette.Green,
    successContainer = MammothPalette.GreenOpacity8,
    info = MammothPalette.Blue,
    infoContainer = MammothPalette.BlueOpacity8,
    dim = MammothPalette.Dim75,
)
