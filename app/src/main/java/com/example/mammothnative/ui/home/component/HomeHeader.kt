package com.example.mammothnative.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mammothnative.R
import com.example.mammothnative.domain.model.ConditionalCopy
import com.example.mammothnative.domain.model.HomeBackgroundSize
import com.example.mammothnative.domain.model.HomeBackgroundType
import com.example.mammothnative.domain.model.HomeSettings
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple
import com.example.mammothnative.ui.util.toColorOrNull

/**
 * 웹 HomeFragment/components/HomeHeader.
 * 배경(색 또는 이미지) 위에 알림 버튼과 상황별 문구를 얹는다.
 */
@Composable
fun HomeHeader(
    settings: HomeSettings?,
    conditionalCopy: ConditionalCopy?,
    hasUnreadNotification: Boolean,
    onNotificationClick: () -> Unit,
    onCopyActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = settings?.backgroundColor?.toColorOrNull()
        ?: MammothTheme.colors.surface.background.bg2
    // 웹: bgSize LARGE 는 1:1, SMALL 은 2:1
    val ratio = if (settings?.backgroundSize == HomeBackgroundSize.LARGE) 1f else 2f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .background(backgroundColor),
    ) {
        if (settings?.backgroundType == HomeBackgroundType.IMAGE && settings.backgroundImageUrl != null) {
            AsyncImage(
                model = settings.backgroundImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        NotificationButton(
            hasUnread = hasUnreadNotification,
            onClick = onNotificationClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            MammothText(
                text = conditionalCopy?.message.orEmpty(),
                style = MammothTheme.typography.headline2,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f, fill = false),
            )
            val label = conditionalCopy?.buttonLabel
            if (label != null) {
                MammothButton(
                    text = label,
                    onClick = onCopyActionClick,
                    size = MammothButtonSize.Xs,
                    variant = MammothButtonVariant.Line,
                    modifier = Modifier.background(Color.White),
                )
            }
        }
    }
}

@Composable
private fun NotificationButton(
    hasUnread: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_alarm),
            contentDescription = "알림",
            tint = MammothTheme.colors.icon.default,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickableNoRipple(onClick = onClick),
        )
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MammothTheme.colors.error),
            )
        }
    }
}
