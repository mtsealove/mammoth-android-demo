package com.example.mammothnative.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.mammothnative.R
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothBorderWidth
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

/** 웹 Gnb 의 `height: calc(58px + ...)` */
private val BarContentHeight = 58.dp
private val OrderCircleSize = 56.dp

/**
 * 웹 MainActivity/components/Gnb.
 *
 * 바 높이는 58dp 로 고정하고 아이템을 아래로 붙인다. 가운데 주문 버튼만 56dp 원이라
 * 바보다 높아져 위로 튀어나온다(웹의 `overflow: visible` 과 같은 모양).
 */
@Composable
fun MammothBottomBar(
    currentTab: MainTab,
    onTabClick: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BarContentHeight + maxOf(16.dp, navigationBarPadding))
            .background(MammothTheme.colors.surface.background.bg1)
            .drawTopBorder()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = maxOf(12.dp, navigationBarPadding),
            ),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        MainTab.entries.forEach { tab ->
            val selected = tab == currentTab
            val label = @Composable {
                MammothText(
                    text = tab.label,
                    style = MammothTheme.typography.label,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) {
                        MammothTheme.colors.text.accent
                    } else {
                        MammothTheme.colors.text.unselected
                    },
                )
            }

            if (tab.isOrder) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        // 바 높이를 넘겨서 측정되도록 풀어준다. 이게 없으면 원이 눌려 들어간다.
                        .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
                        .clickableNoRipple { onTabClick(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    OrderButtonAnimation()
                    label()
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickableNoRipple { onTabClick(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = null,
                        tint = if (selected) {
                            MammothTheme.colors.icon.accent1
                        } else {
                            MammothTheme.colors.icon.unselected
                        },
                        modifier = Modifier.size(24.dp),
                    )
                    label()
                }
            }
        }
    }
}

/** 웹에서 쓰는 것과 같은 Lottie(order_button.json)를 무한 반복 재생한다. */
@Composable
private fun OrderButtonAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.order_button),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    Box(
        modifier = Modifier
            .size(OrderCircleSize)
            .clip(CircleShape),
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(OrderCircleSize),
        )
    }
}

/** 웹 Gnb 의 `border-top: 1px solid` */
@Composable
private fun Modifier.drawTopBorder(): Modifier {
    val color = MammothTheme.colors.icon.disabled
    val strokeWidth = MammothBorderWidth
    return drawBehind {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth.toPx(),
        )
    }
}
