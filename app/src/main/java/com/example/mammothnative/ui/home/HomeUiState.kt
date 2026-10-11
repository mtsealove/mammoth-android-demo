package com.example.mammothnative.ui.home

import com.example.mammothnative.core.Loadable
import com.example.mammothnative.domain.model.ActiveOrder
import com.example.mammothnative.domain.model.Banner
import com.example.mammothnative.domain.model.ConditionalCopy
import com.example.mammothnative.domain.model.HomeSettings
import com.example.mammothnative.domain.model.MyMenu
import com.example.mammothnative.domain.model.RecentOrder
import com.example.mammothnative.domain.model.StampCard

/**
 * 홈 화면이 그리는 데 필요한 모든 상태.
 *
 * 웹에서는 섹션마다 useQuery 가 하나씩 붙어 있었다. 여기서도 섹션별로 [Loadable] 을 따로 둬서
 * 먼저 끝난 섹션부터 그려지게 한다(한 요청이 느려도 화면 전체가 멈추지 않는다).
 */
data class HomeUiState(
    val settings: Loadable<HomeSettings> = Loadable.Loading,
    val conditionalCopy: Loadable<ConditionalCopy> = Loadable.Loading,
    val banners: Loadable<List<Banner>> = Loadable.Loading,
    val isSignedIn: Boolean = false,
    /** 로그인 상태에서만 조회한다. */
    val stampCard: Loadable<StampCard> = Loadable.Loading,
    val activeOrders: List<ActiveOrder> = emptyList(),
    val hasUnreadNotification: Boolean = false,
    val recentOrders: List<RecentOrder> = emptyList(),
    val myMenus: List<MyMenu> = emptyList(),
)
