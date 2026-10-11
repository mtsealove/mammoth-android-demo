package com.example.mammothnative.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme

/** 웹 `components/Badge` 의 state 중 앱에서 쓰는 것들. */
enum class MammothBadgeState { Main, Sub, Blue, Red, Green }

/**
 * 웹 Badge (type=filled 기준: 12sp / medium / radius 4 / padding 4).
 */
@Composable
fun MammothBadge(
    text: String,
    modifier: Modifier = Modifier,
    state: MammothBadgeState = MammothBadgeState.Main,
) {
    val colors = MammothTheme.colors
    val (background, content) = when (state) {
        MammothBadgeState.Main -> colors.primary to colors.text.reverse
        MammothBadgeState.Sub -> colors.primaryContainer to colors.text.secondary
        MammothBadgeState.Blue -> colors.infoContainer to colors.text.link
        MammothBadgeState.Red -> colors.errorContainer to colors.text.error
        MammothBadgeState.Green -> colors.successContainer to colors.text.success
    }

    MammothText(
        text = text,
        style = MammothTheme.typography.label,
        fontWeight = FontWeight.Medium,
        color = content,
        modifier = modifier
            .clip(MammothShapes.sm)
            .background(background)
            .padding(4.dp),
    )
}
