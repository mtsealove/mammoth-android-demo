package com.example.mammothnative.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.mammothnative.R

/**
 * 웹과 동일한 Pretendard.
 * 400/500/600/700 만 번들링하고 800·900 은 가장 가까운 700 로 폴백된다.
 */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

/** 웹의 `line-height: 1.4` 를 그대로 옮긴 값. */
private const val LINE_HEIGHT_RATIO = 1.4f

@OptIn(ExperimentalTextApi::class)
private fun mammothTextStyle(
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
) = TextStyle(
    fontFamily = Pretendard,
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = fontSize * LINE_HEIGHT_RATIO,
    // 웹과 줄 간격/세로 정렬을 맞추기 위해 기본 폰트 패딩을 끄고 행간을 가운데로 분배한다.
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

/**
 * 웹 `components/Text` 의 size 토큰과 1:1 대응.
 * 굵기는 [com.example.mammothnative.ui.component.MammothText] 의 `fontWeight` 로 따로 지정한다.
 */
@Immutable
data class MammothTypography(
    /** 20sp */
    val headline2: TextStyle,
    /** 18sp */
    val headline: TextStyle,
    /** 16sp */
    val title: TextStyle,
    /** 14sp — 기본 본문 */
    val body: TextStyle,
    /** 12sp */
    val label: TextStyle,
    /** 10sp */
    val caption: TextStyle,
)

fun mammothTypography(): MammothTypography = MammothTypography(
    headline2 = mammothTextStyle(20.sp),
    headline = mammothTextStyle(18.sp),
    title = mammothTextStyle(16.sp),
    body = mammothTextStyle(14.sp),
    label = mammothTextStyle(12.sp),
    caption = mammothTextStyle(10.sp),
)
