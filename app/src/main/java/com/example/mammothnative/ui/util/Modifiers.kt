package com.example.mammothnative.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

/**
 * 웹은 `-webkit-tap-highlight-color: transparent` 로 탭 하이라이트를 끈다.
 * 디자인 시스템 버튼 밖의 영역도 같은 느낌이 되도록 ripple 없는 클릭을 쓴다.
 */
fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
}

/** 서버가 내려주는 `#RRGGBB` 문자열을 Color 로 바꾼다. 형식이 다르면 null. */
fun String.toColorOrNull(): Color? = runCatching {
    Color(android.graphics.Color.parseColor(this))
}.getOrNull()

/** 12800 -> "12,800원" */
fun Int.toPriceText(): String = "%,d원".format(this)
