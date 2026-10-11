package com.example.mammothnative.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.domain.model.Banner
import com.example.mammothnative.domain.model.QuickMenuItem
import com.example.mammothnative.ui.component.MammothButton
import com.example.mammothnative.ui.component.MammothButtonSize
import com.example.mammothnative.ui.component.MammothButtonVariant
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.home.component.ActiveOrderCard
import com.example.mammothnative.ui.home.component.BannerItem
import com.example.mammothnative.ui.home.component.ExpressStampCard
import com.example.mammothnative.ui.home.component.HomeHeader
import com.example.mammothnative.ui.home.component.QuickMenuSection
import com.example.mammothnative.ui.theme.MammothSpacing
import com.example.mammothnative.ui.theme.MammothTheme

/**
 * 웹 MainActivity 의 HomeFragment.
 *
 * 구성 순서도 웹과 같다: 헤더 → 익스프레스 스탬프 → 퀵메뉴 → 배너,
 * 그리고 진행 중인 주문 카드가 화면 하단에 떠 있다.
 */
@Composable
fun HomeScreen(
    onQuickMenuClick: (QuickMenuItem) -> Unit,
    onBannerClick: (Banner) -> Unit,
    onSignInClick: () -> Unit,
    onNavigateNotImplemented: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onQuickMenuClick = onQuickMenuClick,
        onBannerClick = onBannerClick,
        onSignInClick = onSignInClick,
        onNavigateNotImplemented = onNavigateNotImplemented,
        onRetry = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onQuickMenuClick: (QuickMenuItem) -> Unit,
    onBannerClick: (Banner) -> Unit,
    onSignInClick: () -> Unit,
    onNavigateNotImplemented: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MammothTheme.colors.surface.background.bg1),
            contentPadding = PaddingValues(bottom = MammothSpacing.xl),
        ) {
            item {
                HomeHeader(
                    settings = uiState.settings.dataOrNull,
                    conditionalCopy = uiState.conditionalCopy.dataOrNull,
                    hasUnreadNotification = uiState.hasUnreadNotification,
                    onNotificationClick = { onNavigateNotImplemented("알림") },
                    onCopyActionClick = {
                        uiState.conditionalCopy.dataOrNull?.menuType?.let { type ->
                            onQuickMenuClick(QuickMenuItem(type, type.label, null, null))
                        }
                    },
                )
            }

            // 비로그인 상태에서는 웹과 같이 스탬프 영역을 띄우지 않는다.
            if (uiState.isSignedIn) {
                item {
                    val stamp = uiState.stampCard
                    if (stamp is Loadable.Success) {
                        ExpressStampCard(
                            stampCard = stamp.data,
                            onClick = { onNavigateNotImplemented("혜택") },
                            modifier = Modifier.padding(
                                start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp,
                            ),
                        )
                    }
                }
            }

            item {
                when (val settings = uiState.settings) {
                    is Loadable.Loading -> LoadingBlock()
                    is Loadable.Failure -> ErrorBlock(
                        throwable = settings.throwable,
                        onRetry = onRetry,
                    )

                    is Loadable.Success -> QuickMenuSection(
                        quickMenuItems = settings.data.quickMenuItems,
                        initiallyExpanded = settings.data.quickMenuExpanded,
                        isSignedIn = uiState.isSignedIn,
                        recentOrders = uiState.recentOrders,
                        myMenus = uiState.myMenus,
                        onShortcutClick = onQuickMenuClick,
                        onMoreClick = { onNavigateNotImplemented("퀵오더") },
                        onSignInClick = onSignInClick,
                        modifier = Modifier.padding(
                            start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp,
                        ),
                    )
                }
            }

            when (val banners = uiState.banners) {
                is Loadable.Loading -> item { LoadingBlock() }
                is Loadable.Failure -> item {
                    ErrorBlock(throwable = banners.throwable, onRetry = onRetry)
                }

                is Loadable.Success -> items(banners.data, key = { it.id }) { banner ->
                    BannerItem(
                        banner = banner,
                        onClick = onBannerClick,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
            }

            // 진행 중인 주문 카드가 마지막 배너를 가리지 않도록 여백을 둔다(웹과 동일).
            if (uiState.activeOrders.isNotEmpty()) {
                item { Box(Modifier.height(104.dp)) }
            }
        }

        if (uiState.activeOrders.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.activeOrders, key = { it.orderNo }) { order ->
                    ActiveOrderCard(
                        order = order,
                        isSingle = uiState.activeOrders.size == 1,
                        onClick = { onNavigateNotImplemented("주문 상세") },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(186.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MammothTheme.colors.primary)
    }
}

@Composable
private fun ErrorBlock(throwable: Throwable, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MammothText(
            text = throwable.message ?: "잠시 후 다시 시도해 주세요",
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
