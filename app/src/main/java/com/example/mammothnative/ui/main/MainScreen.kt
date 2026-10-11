package com.example.mammothnative.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mammothnative.domain.model.OrderType
import com.example.mammothnative.domain.model.QuickMenuType
import com.example.mammothnative.ui.home.HomeScreen
import com.example.mammothnative.ui.menu.MenuScreen
import com.example.mammothnative.ui.shop.ShopScreen
import com.example.mammothnative.ui.placeholder.PlaceholderScreen
import com.example.mammothnative.ui.theme.MammothTheme
import kotlinx.coroutines.launch

/**
 * 웹 MainActivity 대응 화면.
 *
 * 웹은 한 화면 안에서 `fragment` 파라미터로 5개 프래그먼트를 전환했고,
 * 여기서는 Navigation Compose 의 하단 탭으로 같은 구조를 만든다.
 * 탭을 오갈 때 각 탭의 스크롤 위치와 상태는 `saveState/restoreState` 로 유지된다.
 */
@Composable
fun MainScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToItems: (shopId: String, orderType: OrderType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = MainTab.fromRoute(backStackEntry?.destination?.route)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notifyNotImplemented: (String) -> Unit = { label ->
        scope.launch {
            snackbarHostState.showSnackbar("$label 화면은 아직 네이티브로 옮기지 않았어요")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MammothTheme.colors.surface.background.bg1,
        // 홈 헤더가 상태바 뒤까지 올라가야 해서 상단 inset 은 화면이 직접 처리한다.
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MammothBottomBar(
                currentTab = currentTab,
                onTabClick = { tab -> navController.navigateToTab(tab) },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.HOME.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(MainTab.HOME.route) {
                HomeScreen(
                    onQuickMenuClick = { item ->
                        when (item.type) {
                            QuickMenuType.ORDER -> navController.navigateToTab(MainTab.ORDER)
                            QuickMenuType.GIFT -> navController.navigateToTab(MainTab.GIFT)
                            QuickMenuType.EVENT -> navController.navigateToTab(MainTab.EVENT)
                            else -> notifyNotImplemented(item.type.label)
                        }
                    },
                    onBannerClick = { notifyNotImplemented("배너 상세") },
                    onSignInClick = onNavigateToSignIn,
                    onNavigateNotImplemented = notifyNotImplemented,
                )
            }
            composable(MainTab.EVENT.route) {
                PlaceholderScreen(
                    title = "이벤트",
                    description = "웹 EventFragment 를 옮길 자리예요.",
                )
            }
            composable(MainTab.ORDER.route) {
                ShopScreen(
                    onSignInClick = onNavigateToSignIn,
                    onOrderStart = onNavigateToItems,
                )
            }
            composable(MainTab.GIFT.route) {
                PlaceholderScreen(
                    title = "선물하기",
                    description = "웹 GiftFragment 를 옮길 자리예요.",
                )
            }
            composable(MainTab.MENU.route) {
                MenuScreen(onSignInClick = onNavigateToSignIn)
            }
        }
    }
}

/** 탭 전환. 탭 스택을 쌓지 않고 이전 탭의 상태는 보존한다. */
private fun androidx.navigation.NavHostController.navigateToTab(tab: MainTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
