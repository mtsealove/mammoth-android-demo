package com.example.mammothnative.ui.shop.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mammothnative.R
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.domain.model.OrderType
import com.example.mammothnative.domain.model.ShopDetail
import com.example.mammothnative.domain.model.ShopOrderStatus
import com.example.mammothnative.ui.component.MammothBadge
import com.example.mammothnative.ui.component.MammothBadgeState
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonShape
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothModalBottomSheet
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothBorderWidth
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

private val NoteBackground = Color(0xFFFFF1E0)
private val NoteContent = Color(0xFFB06A1E)

/**
 * 웹 activities/shops/ShopBottomSheet.
 * 매장 요약 → 지도 → 주문 전 안내 → 주문 방식 선택 → 닫기 순서가 웹과 같다.
 */
@Composable
fun ShopDetailBottomSheet(
    state: Loadable<ShopDetail>,
    onDismiss: () -> Unit,
    onBookmarkClick: (ShopDetail) -> Unit,
    onOrderTypeClick: (ShopDetail, OrderType) -> Unit,
) {
    MammothModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            when (state) {
                is Loadable.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MammothText(
                        text = "매장 정보를 불러오는 중이에요",
                        color = MammothTheme.colors.text.secondary,
                    )
                }

                is Loadable.Failure -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MammothText(
                        text = state.throwable.message ?: "매장 정보를 불러오지 못했어요",
                        color = MammothTheme.colors.text.error,
                        textAlign = TextAlign.Center,
                    )
                }

                is Loadable.Success -> ShopDetailContent(
                    shop = state.data,
                    onBookmarkClick = onBookmarkClick,
                    onOrderTypeClick = onOrderTypeClick,
                )
            }

            MammothButton(
                text = "닫기",
                onClick = onDismiss,
                size = MammothButtonSize.Lg,
                shape = MammothButtonShape.Square,
                variant = MammothButtonVariant.Text,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun ShopDetailContent(
    shop: ShopDetail,
    onBookmarkClick: (ShopDetail) -> Unit,
    onOrderTypeClick: (ShopDetail, OrderType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShopSummary(shop = shop, onBookmarkClick = onBookmarkClick)
        ShopMap(
            latitude = shop.latitude,
            longitude = shop.longitude,
            name = shop.name,
        )
        ConfirmShopNote()
        OrderTypeSelector(shop = shop, onOrderTypeClick = onOrderTypeClick)
    }
}

@Composable
private fun ShopSummary(
    shop: ShopDetail,
    onBookmarkClick: (ShopDetail) -> Unit,
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                MammothText(
                    text = shop.name,
                    style = MammothTheme.typography.headline2,
                    fontWeight = FontWeight.Bold,
                    ellipsis = true,
                    modifier = Modifier.weight(1f, fill = false),
                )
                shop.kindName?.let {
                    MammothText(
                        text = it,
                        style = MammothTheme.typography.caption,
                        fontWeight = FontWeight.Medium,
                        color = MammothTheme.colors.text.secondary,
                    )
                }
            }
            Icon(
                painter = painterResource(
                    if (shop.isMyShop) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_line,
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

        if (shop.address.isNotBlank()) {
            MammothText(
                text = shop.address,
                style = MammothTheme.typography.label,
                color = MammothTheme.colors.text.secondary,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            shop.kindNote?.let {
                InfoRow(iconRes = R.drawable.ic_info_filled, text = it, isLink = true)
            }
            shop.openHours?.let {
                InfoRow(iconRes = R.drawable.ic_clock, text = it)
            }
            shop.phoneNumber?.let { phone ->
                InfoRow(
                    iconRes = R.drawable.ic_phone,
                    text = phone,
                    underline = true,
                    onClick = {
                        // 웹은 tel: 링크. 앱에서는 다이얼러를 띄운다.
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    iconRes: Int,
    text: String,
    isLink: Boolean = false,
    underline: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = if (onClick != null) Modifier.clickableNoRipple(onClick = onClick) else Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        MammothText(
            text = text,
            color = if (isLink) {
                MammothTheme.colors.text.link
            } else {
                MammothTheme.colors.text.secondary
            },
            underline = underline,
        )
    }
}

@Composable
private fun ConfirmShopNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MammothShapes.md)
            .background(NoteBackground)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info_line),
            contentDescription = null,
            tint = NoteContent,
            modifier = Modifier.size(24.dp),
        )
        MammothText(
            text = "매장명과 주문 내역을 확인해 주세요.\n결제 완료 후에는 변경 및 취소가 불가합니다.",
            style = MammothTheme.typography.label,
            fontWeight = FontWeight.Bold,
            color = NoteContent,
        )
    }
}

@Composable
private fun OrderTypeSelector(
    shop: ShopDetail,
    onOrderTypeClick: (ShopDetail, OrderType) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (shop.useHall) {
            PickupButton(
                iconRes = R.drawable.ic_shop,
                label = "매장",
                enabled = shop.orderEnabled,
                onClick = { onOrderTypeClick(shop, OrderType.IN_SHOP) },
                modifier = Modifier.weight(1f),
            )
        }
        if (shop.useTakeout) {
            PickupButton(
                iconRes = R.drawable.ic_takeout,
                label = "포장",
                enabled = shop.orderEnabled,
                onClick = { onOrderTypeClick(shop, OrderType.TAKE_OUT) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** 웹 components/PickupButton */
@Composable
private fun PickupButton(
    iconRes: Int,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (enabled) {
        MammothTheme.colors.text.primary
    } else {
        MammothTheme.colors.text.disabled
    }
    Column(
        modifier = modifier
            .clip(MammothShapes.sm)
            .background(
                if (enabled) {
                    MammothTheme.colors.surface.background.bg1
                } else {
                    MammothTheme.colors.surface.background.disabled
                },
            )
            .border(MammothBorderWidth, MammothTheme.colors.surface.border.default, MammothShapes.sm)
            .clickableNoRipple(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        MammothText(
            text = label,
            style = MammothTheme.typography.label,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}
