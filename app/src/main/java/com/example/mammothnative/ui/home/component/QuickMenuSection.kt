package com.example.mammothnative.ui.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mammothnative.R
import com.example.mammothnative.domain.model.MyMenu
import com.example.mammothnative.domain.model.QuickMenuItem
import com.example.mammothnative.domain.model.QuickMenuType
import com.example.mammothnative.domain.model.RecentOrder
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

private val ShortcutGradient = Brush.horizontalGradient(
    listOf(Color(0xFF5A4137), Color(0xFF76584C)),
)
private val ShortcutLabelColor = Color(0xFFD7CDC9)
private const val SHORTCUT_COLUMNS = 4

/**
 * 웹 HomeFragment/components/QuickMenu.
 * 위쪽 갈색 카드(바로가기)와 그 위로 겹쳐 올라오는 흰 카드(최근 주문 / MY메뉴)로 이뤄진다.
 */
@Composable
fun QuickMenuSection(
    quickMenuItems: List<QuickMenuItem>,
    initiallyExpanded: Boolean,
    isSignedIn: Boolean,
    recentOrders: List<RecentOrder>,
    myMenus: List<MyMenu>,
    onShortcutClick: (QuickMenuItem) -> Unit,
    onMoreClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ShortcutsCard(
            items = quickMenuItems,
            initiallyExpanded = initiallyExpanded,
            onItemClick = onShortcutClick,
        )
        UserMenusCard(
            isSignedIn = isSignedIn,
            recentOrders = recentOrders,
            myMenus = myMenus,
            onMoreClick = onMoreClick,
            onSignInClick = onSignInClick,
            // 웹의 `margin-bottom: -12px` 와 같이 갈색 카드 위로 겹쳐 올린다.
            modifier = Modifier.offset(y = (-12).dp),
        )
    }
}

@Composable
private fun ShortcutsCard(
    items: List<QuickMenuItem>,
    initiallyExpanded: Boolean,
    onItemClick: (QuickMenuItem) -> Unit,
) {
    var expanded by remember(initiallyExpanded) { mutableStateOf(initiallyExpanded) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(250),
        label = "quickMenuArrow",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(ShortcutGradient),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickableNoRipple { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mammoth_wordmark),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = if (expanded) "바로가기 접기" else "바로가기 펼치기",
                    tint = MammothTheme.colors.icon.reverse,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(arrowRotation),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300)),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items.chunked(SHORTCUT_COLUMNS).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { item ->
                            ShortcutItem(
                                item = item,
                                onClick = onItemClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // 마지막 줄이 4칸을 못 채우면 왼쪽 정렬이 유지되도록 빈 칸을 채운다.
                        repeat(SHORTCUT_COLUMNS - row.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    item: QuickMenuItem,
    onClick: (QuickMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickableNoRipple { onClick(item) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (item.iconUrl != null) {
            AsyncImage(
                model = item.iconUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MammothShapes.lg),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MammothShapes.lg)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(item.type.fallbackIconRes()),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        MammothText(
            text = item.name,
            style = MammothTheme.typography.label,
            fontWeight = FontWeight.Medium,
            color = ShortcutLabelColor,
        )
    }
}

/** 서버가 아이콘을 안 내려주는 메뉴용 기본 아이콘. */
private fun QuickMenuType.fallbackIconRes(): Int = when (this) {
    QuickMenuType.GIFT -> R.drawable.ic_nav_gift
    QuickMenuType.EVENT, QuickMenuType.FREQUENCY, QuickMenuType.ROULETTE_EVENT ->
        R.drawable.ic_nav_event
    QuickMenuType.ORDER, QuickMenuType.ORDER_HISTORY, QuickMenuType.MY_MENU ->
        R.drawable.ic_nav_order
    else -> R.drawable.ic_nav_all_menu
}

private enum class UserMenuTab(val label: String) {
    RECENT_ORDER("최근 주문"),
    MY_MENU("MY메뉴"),
}

@Composable
private fun UserMenusCard(
    isSignedIn: Boolean,
    recentOrders: List<RecentOrder>,
    myMenus: List<MyMenu>,
    onMoreClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(UserMenuTab.RECENT_ORDER) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MammothShapes.lg)
            .background(MammothTheme.colors.surface.background.bg1)
            .defaultMinSize(minHeight = 186.dp)
            .padding(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UserMenuTab.entries.forEach { entry ->
                    val selected = tab == entry
                    MammothText(
                        text = entry.label,
                        style = MammothTheme.typography.headline,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) {
                            MammothTheme.colors.text.primary
                        } else {
                            MammothTheme.colors.text.unselected
                        },
                        modifier = Modifier.clickableNoRipple { tab = entry },
                    )
                }
            }
            if (isSignedIn) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = "더보기",
                    tint = MammothTheme.colors.text.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickableNoRipple(onClick = onMoreClick),
                )
            }
        }

        if (!isSignedIn) {
            SignInPrompt(onSignInClick = onSignInClick)
        } else {
            when (tab) {
                UserMenuTab.RECENT_ORDER -> QuickOrderRow(
                    items = recentOrders.map { it.orderName to "${it.shopName} · ${it.paymentAmount.toPriceText()}" },
                    emptyMessage = "최근 주문 내역이 없어요",
                )

                UserMenuTab.MY_MENU -> QuickOrderRow(
                    items = myMenus.map { it.name.ifBlank { it.itemName } to it.shopName },
                    emptyMessage = "저장한 MY메뉴가 없어요",
                )
            }
        }
    }
}

@Composable
private fun SignInPrompt(onSignInClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MammothText(text = "로그인 후 최근 주문과 MY메뉴를 사용할 수 있어요")
        MammothButton(
            text = "로그인 하러 가기",
            onClick = onSignInClick,
            size = MammothButtonSize.Xs,
        )
    }
}

@Composable
private fun QuickOrderRow(
    items: List<Pair<String, String>>,
    emptyMessage: String,
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            MammothText(
                text = emptyMessage,
                color = MammothTheme.colors.text.unselected,
            )
        }
        return
    }
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items) { (title, subtitle) ->
            Column(
                modifier = Modifier
                    .clip(MammothShapes.md)
                    .background(MammothTheme.colors.surface.background.bg2)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MammothText(
                    text = title,
                    style = MammothTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    ellipsis = true,
                )
                MammothText(
                    text = subtitle,
                    style = MammothTheme.typography.label,
                    color = MammothTheme.colors.text.secondary,
                    ellipsis = true,
                )
            }
        }
    }
}

/** 12800 -> "12,800원" */
private fun Int.toPriceText(): String = "%,d원".format(this)
