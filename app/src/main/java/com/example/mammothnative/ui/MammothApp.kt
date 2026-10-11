package com.example.mammothnative.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mammothnative.domain.model.OrderType
import com.example.mammothnative.ui.auth.PhoneSignInScreen
import com.example.mammothnative.ui.auth.SignInScreen
import com.example.mammothnative.ui.items.ItemScreen
import com.example.mammothnative.ui.items.ItemsScreen
import com.example.mammothnative.ui.main.MainScreen

/**
 * 앱 최상위 네비게이션.
 * 하단 탭이 있는 [MainScreen] 과, 그 위를 덮는 로그인·메뉴 화면들을 구분한다.
 */
@Composable
fun MammothApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.MAIN) {
        composable(Route.MAIN) {
            MainScreen(
                onNavigateToSignIn = { navController.navigate(Route.SIGN_IN) },
                onNavigateToItems = { shopId, orderType ->
                    navController.navigate(Route.items(shopId, orderType))
                },
            )
        }
        composable(Route.SIGN_IN) {
            SignInScreen(
                onBack = { navController.popBackStack() },
                onPhoneSignInClick = { navController.navigate(Route.PHONE_SIGN_IN) },
            )
        }
        composable(Route.PHONE_SIGN_IN) {
            PhoneSignInScreen(
                onBack = { navController.popBackStack() },
                // 로그인에 성공하면 로그인 화면들을 걷어내고 이전 화면으로 돌아간다.
                onSignedIn = { navController.popBackStack(Route.SIGN_IN, inclusive = true) },
            )
        }
        composable(
            route = Route.ITEMS,
            arguments = listOf(
                navArgument("shopId") { type = NavType.StringType },
                navArgument("orderType") { type = NavType.StringType },
            ),
        ) { entry ->
            val shopId = entry.arguments?.getString("shopId").orEmpty()
            val orderType = entry.arguments?.getString("orderType").orEmpty()
            ItemsScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { itemId ->
                    navController.navigate(Route.item(shopId, itemId, orderType))
                },
                onCartClick = { /* 장바구니 화면은 아직 없다 */ },
            )
        }
        composable(
            route = Route.ITEM,
            arguments = listOf(
                navArgument("shopId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType },
                navArgument("orderType") { type = NavType.StringType },
            ),
        ) {
            ItemScreen(
                onBack = { navController.popBackStack() },
                onSignInRequired = { navController.navigate(Route.SIGN_IN) },
                onAddedToCart = { navController.popBackStack() },
            )
        }
    }
}

private object Route {
    const val MAIN = "main"
    const val SIGN_IN = "signIn"
    const val PHONE_SIGN_IN = "signIn/phone"
    const val ITEMS = "shop/{shopId}/items?orderType={orderType}"
    const val ITEM = "shop/{shopId}/item/{itemId}?orderType={orderType}"

    fun items(shopId: String, orderType: OrderType) =
        "shop/$shopId/items?orderType=${orderType.name}"

    fun item(shopId: String, itemId: String, orderType: String) =
        "shop/$shopId/item/$itemId?orderType=$orderType"
}
