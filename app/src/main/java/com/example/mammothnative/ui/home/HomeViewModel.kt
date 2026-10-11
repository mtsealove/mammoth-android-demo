package com.example.mammothnative.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.data.remote.TokenStore
import com.example.mammothnative.data.repository.BannerRepository
import com.example.mammothnative.data.repository.HomeRepository
import com.example.mammothnative.data.repository.MyMenuRepository
import com.example.mammothnative.data.repository.NotificationRepository
import com.example.mammothnative.data.repository.OrderRepository
import com.example.mammothnative.data.repository.StampRepository
import com.example.mammothnative.domain.model.BannerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 홈의 서버 상태를 모아 하나의 [HomeUiState] 로 노출한다.
 *
 * 웹의 react-query 자리를 ViewModel + StateFlow 가 대신한다.
 * 화면 회전처럼 Composable 이 다시 만들어지는 상황에서도 ViewModel 은 살아 있으므로
 * 이미 받아온 데이터를 다시 요청하지 않는다(쿼리 캐시와 같은 효과).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val bannerRepository: BannerRepository,
    private val orderRepository: OrderRepository,
    private val stampRepository: StampRepository,
    private val myMenuRepository: MyMenuRepository,
    private val notificationRepository: NotificationRepository,
    tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPublicSections()
        viewModelScope.launch {
            tokenStore.isSignedIn.distinctUntilChanged().collect { signedIn ->
                _uiState.update { it.copy(isSignedIn = signedIn) }
                if (signedIn) {
                    loadSignedInSections()
                } else {
                    _uiState.update {
                        it.copy(
                            stampCard = Loadable.Loading,
                            activeOrders = emptyList(),
                            recentOrders = emptyList(),
                            myMenus = emptyList(),
                            hasUnreadNotification = false,
                        )
                    }
                }
            }
        }
    }

    /** 당겨서 새로고침·탭 재진입처럼 다시 불러와야 할 때 호출한다. */
    fun refresh() {
        loadPublicSections()
        if (_uiState.value.isSignedIn) {
            loadSignedInSections()
        }
    }

    private fun loadPublicSections() {
        load(
            setState = { state, value -> state.copy(settings = value) },
            block = { homeRepository.getHomeSettings() },
        )
        load(
            setState = { state, value -> state.copy(conditionalCopy = value) },
            block = { homeRepository.getConditionalCopy() },
        )
        load(
            setState = { state, value -> state.copy(banners = value) },
            block = { bannerRepository.getBanners(BannerType.MAIN_VERTICAL).take(MAX_BANNERS) },
        )
    }

    private fun loadSignedInSections() {
        load(
            setState = { state, value -> state.copy(stampCard = value) },
            block = { stampRepository.getExpressStampCard() },
        )
        viewModelScope.launch {
            // 실패해도 홈의 다른 영역에는 영향을 주지 않는 보조 정보라 조용히 넘긴다.
            runCatching { orderRepository.getActiveOrders() }
                .onSuccess { orders -> _uiState.update { it.copy(activeOrders = orders) } }
        }
        viewModelScope.launch {
            runCatching { notificationRepository.hasUnread() }
                .onSuccess { unread -> _uiState.update { it.copy(hasUnreadNotification = unread) } }
        }
        viewModelScope.launch {
            runCatching { orderRepository.getRecentOrders() }
                .onSuccess { orders -> _uiState.update { it.copy(recentOrders = orders) } }
        }
        viewModelScope.launch {
            runCatching { myMenuRepository.getMyMenus() }
                .onSuccess { menus -> _uiState.update { it.copy(myMenus = menus) } }
        }
    }

    /** 섹션 하나를 불러와 로딩 → 성공/실패까지 상태에 반영한다. */
    private fun <T> load(
        setState: (HomeUiState, Loadable<T>) -> HomeUiState,
        block: suspend () -> T,
    ) {
        viewModelScope.launch {
            _uiState.update { setState(it, Loadable.Loading) }
            val result = runCatching { block() }
            _uiState.update {
                setState(
                    it,
                    result.fold(
                        onSuccess = { value -> Loadable.Success(value) },
                        onFailure = { error -> Loadable.Failure(error) },
                    ),
                )
            }
        }
    }

    private companion object {
        /** 웹 Banners 의 `slice(0, 10)` */
        const val MAX_BANNERS = 10
    }
}
