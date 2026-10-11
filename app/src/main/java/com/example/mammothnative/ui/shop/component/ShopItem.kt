package com.example.mammothnative.ui.shop.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mammothnative.R
import com.example.mammothnative.domain.model.Shop
import com.example.mammothnative.domain.model.ShopOrderStatus
import com.example.mammothnative.ui.component.MammothBadge
import com.example.mammothnative.ui.component.MammothBadgeState
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

/**
 * 웹 components/shops/ShopItem.
 * 썸네일 + (이름·영업상태·즐겨찾기) + 주소 + 거리/도보시간 + 배지.
 */
@Composable
fun ShopItem(
    shop: Shop,
    onClick: (Shop) -> Unit,
    onBookmarkClick: (Shop) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickableNoRipple { onClick(shop) },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (shop.thumbnailUrl != null) {
            AsyncImage(
                model = shop.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MammothShapes.md),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MammothShapes.md)
                    .background(MammothTheme.colors.surface.background.bg2),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 이름 + 영업상태를 한 묶음으로 왼쪽에 두고 남은 폭을 모두 차지하게 한다.
                    // (웹 ShopItem 의 `flex: 1; overflow: hidden` 행)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MammothText(
                            text = shop.name,
                            style = MammothTheme.typography.title,
                            fontWeight = FontWeight.Medium,
                            ellipsis = true,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (shop.orderStatus != ShopOrderStatus.UNKNOWN) {
                            MammothBadge(
                                text = shop.orderStatus.label,
                                state = if (shop.orderStatus == ShopOrderStatus.OPEN) {
                                    MammothBadgeState.Blue
                                } else {
                                    MammothBadgeState.Sub
                                },
                            )
                        }
                    }
                    // 북마크는 항상 오른쪽 끝에 붙인다.
                    Icon(
                        painter = painterResource(
                            if (shop.isMyShop) {
                                R.drawable.ic_bookmark_filled
                            } else {
                                R.drawable.ic_bookmark_line
                            },
                        ),
                        contentDescription = if (shop.isMyShop) "MY매장 해제" else "MY매장 등록",
                        tint = if (shop.isMyShop) {
                            MammothTheme.colors.icon.default
                        } else {
                            MammothTheme.colors.icon.unselected
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .clickableNoRipple { onBookmarkClick(shop) },
                    )
                }

                MammothText(
                    text = shop.address,
                    style = MammothTheme.typography.label,
                    color = MammothTheme.colors.text.secondary,
                    ellipsis = true,
                )

                if (shop.distanceText != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconText(iconRes = R.drawable.ic_map_pin, text = shop.distanceText)
                        shop.walkMinutes?.let {
                            IconText(iconRes = R.drawable.ic_walk_time, text = "$it 분")
                        }
                    }
                }
            }

            if (shop.badges.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    shop.badges.take(MAX_BADGES).forEach { badge ->
                        MammothBadge(text = badge, state = MammothBadgeState.Sub)
                    }
                }
            }
        }
    }
}

@Composable
private fun IconText(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        MammothText(
            text = text,
            style = MammothTheme.typography.label,
            color = MammothTheme.colors.text.secondary,
        )
    }
}

private const val MAX_BADGES = 3
