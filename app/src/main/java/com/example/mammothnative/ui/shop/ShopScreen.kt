package com.example.mammothnative.ui.shop

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mammothnative.R
import com.example.mammothnative.data.location.LocationProvider
import com.example.mammothnative.domain.model.OrderType
import com.example.mammothnative.domain.model.Shop
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.component.MammothTextField
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.ui.shop.component.ShopDetailBottomSheet
import com.example.mammothnative.ui.shop.component.ShopItem
import com.example.mammothnative.ui.theme.MammothBorderWidth
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

/**
 * 웹 MainActivity 의 ShopFragment.
 *
 * 주변 매장은 좌표가 필요하므로, 화면이 열리면 위치 권한을 확인하고
 * 좌표(또는 거부 시 빈 좌표)를 확보한 뒤에 조회를 시작한다.
 */
@Composable
fun ShopScreen(
    onSignInClick: () -> Unit,
    onOrderStart: (shopId: String, orderType: OrderType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShopViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        viewModel.onLocationPermissionResult(result.values.any { it })
    }

    // 웹과 같이 매장 탭에 들어왔을 때만 권한 팝업을 띄운다.
    LaunchedEffect(Unit) {
        val granted = LocationProvider.LOCATION_PERMISSIONS.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) {
            viewModel.onScreenShown()
        } else {
            permissionLauncher.launch(LocationProvider.LOCATION_PERMISSIONS)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MammothTheme.colors.surface.background.bg1)
            .statusBarsPadding(),
    ) {
        ShopTabRow(
            tabs = ShopTab.entries.filter { !it.authRequired || uiState.isSignedIn },
            selected = uiState.tab,
            onSelect = viewModel::onTabSelect,
        )

        when (uiState.tab) {
            ShopTab.QUICK_ORDER -> QuickOrderPlaceholder(modifier = Modifier.weight(1f))

            // Column 의 자식은 기본적으로 높이 제약이 무한이라 LazyColumn 이 모든 항목을
            // 한 번에 구성해버린다(= 다음 페이지가 끝없이 호출된다). weight 로 높이를 확정한다.
            else -> ShopListSection(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                onShopClick = { shop -> viewModel.openShopDetail(shop.id) },
                onBookmarkClick = { shop ->
                    if (uiState.isSignedIn) viewModel.toggleMyShop(shop) else onSignInClick()
                },
                onQueryChange = viewModel::onQueryChange,
                onSearchToggle = viewModel::onSearchToggle,
                onRefresh = viewModel::refresh,
                onLoadMore = viewModel::loadMore,
            )
        }
    }

    uiState.shopDetail?.let { detail ->
        ShopDetailBottomSheet(
            state = detail,
            onDismiss = viewModel::closeShopDetail,
            onBookmarkClick = { shop ->
                if (uiState.isSignedIn) viewModel.toggleMyShopDetail(shop) else onSignInClick()
            },
            onOrderTypeClick = { shop, orderType ->
                viewModel.closeShopDetail()
                onOrderStart(shop.id, orderType)
            },
        )
    }
}

@Composable
private fun ShopTabRow(
    tabs: List<ShopTab>,
    selected: ShopTab,
    onSelect: (ShopTab) -> Unit,
) {
    val borderColor = MammothTheme.colors.surface.border.default
    val accentColor = MammothTheme.colors.text.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = MammothBorderWidth.toPx(),
                )
            },
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickableNoRipple { onSelect(tab) }
                    .drawBehind {
                        if (isSelected) {
                            drawLine(
                                color = accentColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                MammothText(
                    text = tab.label,
                    style = MammothTheme.typography.title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                        MammothTheme.colors.text.primary
                    } else {
                        MammothTheme.colors.text.unselected
                    },
                )
            }
        }
    }
}

@Composable
private fun ShopListSection(
    modifier: Modifier = Modifier,
    uiState: ShopUiState,
    onShopClick: (Shop) -> Unit,
    onBookmarkClick: (Shop) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    val list = uiState.currentList
    val isNearTab = uiState.tab == ShopTab.NEAR

    // 끝에서 세 번째 항목이 보이면 다음 페이지를 미리 불러온다.
    val shouldLoadMore by remember(list.shops.size, list.isLast) {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            list.shops.isNotEmpty() && !list.isLast && lastVisible >= list.shops.size - 3
        }
    }
    LaunchedEffect(listState, list.shops.size, list.isLast) {
        snapshotFlow { shouldLoadMore }.collect { if (it) onLoadMore() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (isNearTab) {
            SearchRow(
                query = uiState.query,
                isSearchOpen = uiState.isSearchOpen,
                onQueryChange = onQueryChange,
                onSearchToggle = onSearchToggle,
                onRefresh = onRefresh,
            )
            if (uiState.isLocationUnavailable) {
                MammothText(
                    text = "위치 정보를 쓸 수 없어 거리순으로 정렬되지 않아요.",
                    style = MammothTheme.typography.label,
                    color = MammothTheme.colors.text.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }

        when {
            uiState.location == null && isNearTab -> LoadingBlock()

            list.errorMessage != null -> ErrorBlock(message = list.errorMessage, onRetry = onRefresh)

            list.isLoading -> LoadingBlock()

            list.isBlank -> BlankBlock(
                message = if (isNearTab) "주변에 매장이 없어요" else "자주 가는 매장을 설정할 수 있어요!",
            )

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(list.shops, key = { it.id }) { shop ->
                    ShopItem(
                        shop = shop,
                        onClick = onShopClick,
                        onBookmarkClick = onBookmarkClick,
                    )
                }
                if (list.isLoadingMore) {
                    item { LoadingBlock(height = 60.dp) }
                }
            }
        }
    }
}

@Composable
private fun SearchRow(
    query: String,
    isSearchOpen: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSearchOpen) {
            MammothTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "매장 이름을 검색해 주세요",
                modifier = Modifier.weight(1f),
            )
            MammothButton(
                text = "취소",
                onClick = { onSearchToggle(false) },
                size = MammothButtonSize.Xs,
                variant = MammothButtonVariant.Text,
            )
        } else {
            MammothText(
                text = "내 위치 주변",
                style = MammothTheme.typography.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            MammothButton(
                text = "현재 위치로",
                onClick = onRefresh,
                size = MammothButtonSize.Xs,
                variant = MammothButtonVariant.Line,
            )
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "매장 검색",
                tint = MammothTheme.colors.icon.default,
                modifier = Modifier
                    .size(24.dp)
                    .clickableNoRipple { onSearchToggle(true) },
            )
        }
    }
}

@Composable
private fun QuickOrderPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        MammothText(
            text = "퀵오더(최근 주문·MY메뉴)는 아직 옮기지 않았어요.",
            color = MammothTheme.colors.text.secondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LoadingBlock(height: androidx.compose.ui.unit.Dp = 200.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MammothTheme.colors.primary)
    }
}

@Composable
private fun BlankBlock(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        MammothText(text = message, color = MammothTheme.colors.text.unselected)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MammothText(
            text = message,
            color = MammothTheme.colors.text.secondary,
            textAlign = TextAlign.Center,
        )
        MammothButton(
            text = "다시 불러오기",
            onClick = onRetry,
            size = MammothButtonSize.Xs,
            variant = MammothButtonVariant.Line,
        )
    }
}
