package com.example.mammothnative.ui.main

import androidx.annotation.DrawableRes
import com.example.mammothnative.R

/**
 * 웹 `MainFragment` 와 Gnb 구성 대응.
 * 웹에서는 MainActivity 의 `fragment` 파라미터로 전환했고, 여기서는 하단 탭의 목적지가 된다.
 */
enum class MainTab(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int,
    /** 가운데 동그란 주문 버튼 */
    val isOrder: Boolean = false,
    /** 로그인해야 들어갈 수 있는 탭 */
    val authRequired: Boolean = false,
) {
    HOME("home", "홈", R.drawable.ic_nav_home),
    EVENT("event", "이벤트", R.drawable.ic_nav_event, authRequired = true),
    ORDER("order", "주문", R.drawable.ic_nav_order, isOrder = true),
    GIFT("gift", "선물하기", R.drawable.ic_nav_gift, authRequired = true),
    MENU("menu", "전체메뉴", R.drawable.ic_nav_all_menu);

    companion object {
        fun fromRoute(route: String?) = entries.firstOrNull { it.route == route } ?: HOME
    }
}
