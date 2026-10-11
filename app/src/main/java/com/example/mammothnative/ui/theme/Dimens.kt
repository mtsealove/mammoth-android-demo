package com.example.mammothnative.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** 웹에서 반복적으로 쓰이는 간격 값. */
object MammothSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    /** 화면 좌우 기본 여백 (웹 `margin: 0 18px`) */
    val screenHorizontal = 18.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object MammothRadius {
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    /** 완전 둥근(pill) 형태 */
    val pill = 100.dp
}

object MammothShapes {
    val sm = RoundedCornerShape(MammothRadius.sm)
    val md = RoundedCornerShape(MammothRadius.md)
    val lg = RoundedCornerShape(MammothRadius.lg)
    val xl = RoundedCornerShape(MammothRadius.xl)
    val pill = RoundedCornerShape(MammothRadius.pill)
    val none = RoundedCornerShape(0.dp)
}

/** 테두리 두께 (웹 `1px solid`) */
val MammothBorderWidth = 1.dp
