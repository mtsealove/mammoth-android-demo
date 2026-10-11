package com.example.mammothnative.ui.home.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mammothnative.domain.model.StampCard
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

/**
 * 웹 HomeFragment/components/ExpressStamp.
 * 진행 바는 진입 직후 0 에서 현재 값으로 차오른다.
 */
@Composable
fun ExpressStampCard(
    stampCard: StampCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(stampCard) { animated = true }
    val progress by animateFloatAsState(
        targetValue = if (animated) stampCard.progress else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "stampProgress",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MammothShapes.md)
            .background(MammothTheme.colors.surface.background.bg1)
            .clickableNoRipple(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MammothText(
            text = "매머드익스프레스 스탬프",
            style = MammothTheme.typography.label,
            color = MammothTheme.colors.text.secondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MammothText(
                text = stampCard.message,
                style = MammothTheme.typography.title,
                fontWeight = FontWeight.Bold,
                color = MammothTheme.colors.text.secondary,
            )
            MammothText(
                text = "${stampCard.stampCount} / ${stampCard.totalSlots}",
                style = MammothTheme.typography.label,
                fontWeight = FontWeight.Medium,
                color = MammothTheme.colors.text.secondary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MammothShapes.sm)
                .background(MammothTheme.colors.icon.disabled),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(MammothShapes.sm)
                    .background(MammothTheme.colors.primary),
            )
        }
    }
}
