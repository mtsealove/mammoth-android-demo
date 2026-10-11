package com.example.mammothnative.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mammothnative.R
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.domain.model.ItemCategory
import com.example.mammothnative.domain.model.MenuItem
import com.example.mammothnative.domain.model.OrderType
import com.example.mammothnative.ui.component.MammothBadge
import com.example.mammothnative.ui.component.MammothBadgeState
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple
import com.example.mammothnative.ui.util.toPriceText

/**
 * 웹 ItemsActivity.
 * 상단 헤더(매장 이름·장바구니) → 카테고리 탭 → (하위 카테고리 칩) → 메뉴 목록,
 * 하단에 주문 방식과 매장명을 보여주는 바가 있다.
 */
@Composable
fun ItemsScreen(
    onBack: () -> Unit,
    onItemClick: (itemId: String) -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 메뉴를 담고 돌아오면 장바구니 배지를 다시 맞춘다.
    LifecycleResumeEffect(Unit) {
        viewModel.refreshCartCount()
        onPauseOrDispose { }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MammothTheme.colors.surface.background.bg1)
            .statusBarsPadding(),
    ) {
        ItemsHeader(
            cartCount = uiState.cartCount,
            onBack = onBack,
            onCartClick = onCartClick,
        )

        when (val categories = uiState.categories) {
            is Loadable.Loading -> LoadingBlock(modifier = Modifier.weight(1f))

            is Loadable.Failure -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                MammothText(
                    text = categories.throwable.message ?: "메뉴를 불러오지 못했어요",
                    color = MammothTheme.colors.text.secondary,
                    textAlign = TextAlign.Center,
                )
            }

            is Loadable.Success -> {
                CategoryTabs(
                    categories = categories.data,
                    selectedIndex = uiState.selectedCategoryIndex,
                    onSelect = viewModel::onCategorySelect,
                )
                uiState.selectedCategory?.takeIf { it.usesSubCategory }?.let { category ->
                    SubCategoryChips(
                        category = category,
                        selectedIndex = uiState.selectedSubCategoryIndex,
                        onSelect = viewModel::onSubCategorySelect,
                    )
                }
                ItemsList(
                    items = uiState.items,
                    orderType = uiState.orderType,
                    onItemClick = onItemClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ItemsFooter(shopName = uiState.shopName, orderType = uiState.orderType)
    }
}

@Composable
private fun ItemsHeader(
    cartCount: Int,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_left),
            contentDescription = "뒤로",
            tint = MammothTheme.colors.icon.default,
            modifier = Modifier
                .size(24.dp)
                .clickableNoRipple(onClick = onBack),
        )
        Box {
            Icon(
                painter = painterResource(R.drawable.ic_cart),
                contentDescription = "장바구니",
                tint = MammothTheme.colors.icon.default,
                modifier = Modifier
                    .size(24.dp)
                    .clickableNoRipple(onClick = onCartClick),
            )
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MammothTheme.colors.error),
                    contentAlignment = Alignment.Center,
                ) {
                    MammothText(
                        text = "$cartCount",
                        style = MammothTheme.typography.caption,
                        fontWeight = FontWeight.Medium,
                        color = MammothTheme.colors.text.reverse,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<ItemCategory>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(categories, key = { _, item -> item.id }) { index, category ->
            val selected = index == selectedIndex
            MammothText(
                text = category.name,
                style = MammothTheme.typography.title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MammothTheme.colors.text.primary
                } else {
                    MammothTheme.colors.text.unselected
                },
                modifier = Modifier
                    .clickableNoRipple { onSelect(index) }
                    .padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun SubCategoryChips(
    category: ItemCategory,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(category.children, key = { _, item -> item.id }) { index, sub ->
            val selected = index == selectedIndex
            MammothText(
                text = sub.name,
                style = MammothTheme.typography.label,
                fontWeight = FontWeight.Medium,
                color = if (selected) {
                    MammothTheme.colors.text.reverse
                } else {
                    MammothTheme.colors.text.secondary
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (selected) {
                            MammothTheme.colors.primary
                        } else {
                            MammothTheme.colors.surface.background.bg2
                        },
                    )
                    .clickableNoRipple { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun ItemsList(
    items: Loadable<List<MenuItem>>,
    orderType: OrderType,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (items) {
        is Loadable.Loading -> LoadingBlock(modifier = modifier)

        is Loadable.Failure -> Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            MammothText(
                text = items.throwable.message ?: "메뉴를 불러오지 못했어요",
                color = MammothTheme.colors.text.secondary,
            )
        }

        is Loadable.Success -> if (items.data.isEmpty()) {
            Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MammothText(
                    text = "판매 중인 메뉴가 없어요",
                    color = MammothTheme.colors.text.unselected,
                )
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(items.data, key = { it.id }) { item ->
                    MenuItemRow(
                        item = item,
                        orderType = orderType,
                        onClick = { if (!item.isSoldOut) onItemClick(item.id) },
                    )
                }
            }
        }
    }
}

/** 웹 ItemSection/Item */
@Composable
private fun MenuItemRow(
    item: MenuItem,
    orderType: OrderType,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(enabled = !item.isSoldOut, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                // 품절은 흑백 처리(웹 grayscale)
                colorFilter = if (item.isSoldOut) {
                    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                } else {
                    null
                },
                modifier = Modifier
                    .size(80.dp)
                    .clip(MammothShapes.md)
                    .background(MammothTheme.colors.surface.background.bg2),
            )
            if (item.isSoldOut) {
                MammothBadge(text = "품절", state = MammothBadgeState.Sub)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (item.badges.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.badges.take(2).forEach { badge ->
                        MammothText(
                            text = badge,
                            style = MammothTheme.typography.label,
                            fontWeight = FontWeight.Medium,
                            color = MammothTheme.colors.text.error,
                        )
                    }
                }
            }
            MammothText(
                text = item.name,
                style = MammothTheme.typography.title,
                fontWeight = FontWeight.Medium,
            )
            item.description?.let {
                MammothText(
                    text = it,
                    style = MammothTheme.typography.label,
                    color = MammothTheme.colors.text.secondary,
                )
            }
            MammothText(
                text = item.priceFor(orderType).toPriceText(),
                style = MammothTheme.typography.title,
            )
        }
    }
}

/** 웹 ItemsFooter: 주문 방식 + 매장 이름 */
@Composable
private fun ItemsFooter(shopName: String, orderType: OrderType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(MammothTheme.colors.surface.button.default)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MammothText(
            text = orderType.label,
            style = MammothTheme.typography.label,
            fontWeight = FontWeight.Medium,
            color = MammothTheme.colors.text.accent,
            modifier = Modifier
                .clip(MammothShapes.sm)
                .background(MammothTheme.colors.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        MammothText(
            text = shopName,
            style = MammothTheme.typography.title,
            fontWeight = FontWeight.Medium,
            color = MammothTheme.colors.text.reverse,
            ellipsis = true,
        )
    }
}

@Composable
private fun LoadingBlock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MammothTheme.colors.primary)
    }
}
