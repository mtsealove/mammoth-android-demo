package com.example.mammothnative.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mammothnative.data.location.LocationProvider
import com.example.mammothnative.data.remote.TokenStore
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.data.repository.ShopRepository
import com.example.mammothnative.domain.model.Shop
import com.example.mammothnative.domain.model.ShopDetail
import com.example.mammothnative.domain.model.ShopPage
import com.example.mammothnative.domain.model.UserLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ShopTab(val label: String, val authRequired: Boolean) {
    NEAR("주변매장", false),
    MY("MY매장", true),
    QUICK_ORDER("퀵오더", true),
}

data class ShopListState(
    val shops: List<Shop> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLast: Boolean = false,
    val errorMessage: String? = null,
) {
    val isBlank: Boolean get() = !isLoading && errorMessage == null && shops.isEmpty()
}

data class ShopUiState(
    val tab: ShopTab = ShopTab.NEAR,
    val isSignedIn: Boolean = false,
    val query: String = "",
    val isSearchOpen: Boolean = false,
    /** null 이면 아직 좌표를 확보하기 전(조회를 시작하지 않는다). */
    val location: UserLocation? = null,
    val nearShops: ShopListState = ShopListState(),
    val myShops: ShopListState = ShopListState(),
    /** null 이면 매장 상세 시트가 닫혀 있다. */
    val shopDetail: Loadable<ShopDetail>? = null,
) {
    val isLocationUnavailable: Boolean get() = location?.agreed == false
    val currentList: ShopListState
        get() = if (tab == ShopTab.MY) myShops else nearShops
}

/**
 * 웹 ShopFragment(주변매장 / MY매장 / 퀵오더)의 상태.
 *
 * 주변 매장은 좌표가 준비된 뒤에 조회한다. 웹도 (0,0)으로 먼저 조회하지 않도록
 * `isPositionReady` 로 기다렸다가 호출한다.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val locationProvider: LocationProvider,
    tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    private val queryInput = MutableStateFlow("")

    init {
        viewModelScope.launch {
            tokenStore.isSignedIn.distinctUntilChanged().collect { signedIn ->
                _uiState.update { state ->
                    // 로그아웃하면 로그인 전용 탭에 머무를 수 없다.
                    val tab = if (!signedIn && state.tab.authRequired) ShopTab.NEAR else state.tab
                    state.copy(isSignedIn = signedIn, tab = tab, myShops = ShopListState())
                }
                if (signedIn && _uiState.value.tab == ShopTab.MY) loadMyShops(reset = true)
            }
        }
        viewModelScope.launch {
            // 검색어는 입력이 멈춘 뒤에 조회한다(웹 useDebounce).
            queryInput.drop(1).debounce(SEARCH_DEBOUNCE_MS).distinctUntilChanged()
                .collect { query ->
                    _uiState.update { it.copy(query = query) }
                    if (_uiState.value.location != null) loadNearShops(reset = true)
                }
        }
    }

    /** 매장 탭이 보일 때 호출한다. 권한이 이미 있으면 바로 좌표를 받아 조회를 시작한다. */
    fun onScreenShown() {
        if (_uiState.value.location != null) return
        if (locationProvider.hasPermission()) {
            resolveLocation()
        }
    }

    /** 권한 요청 결과. 거부돼도 웹과 같이 좌표 없이 조회한다. */
    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) {
            resolveLocation()
        } else {
            applyLocation(UserLocation.Unavailable)
        }
    }

    fun onTabSelect(tab: ShopTab) {
        if (_uiState.value.tab == tab) return
        _uiState.update { it.copy(tab = tab) }
        when (tab) {
            ShopTab.MY -> if (_uiState.value.myShops.shops.isEmpty()) loadMyShops(reset = true)
            ShopTab.NEAR -> if (_uiState.value.nearShops.shops.isEmpty() &&
                _uiState.value.location != null
            ) {
                loadNearShops(reset = true)
            }

            ShopTab.QUICK_ORDER -> Unit
        }
    }

    fun onQueryChange(query: String) {
        queryInput.value = query
        _uiState.update { it.copy(query = query) }
    }

    fun onSearchToggle(open: Boolean) {
        _uiState.update { it.copy(isSearchOpen = open) }
        if (!open && _uiState.value.query.isNotEmpty()) onQueryChange("")
    }

    /** 목록 끝에 닿았을 때 다음 페이지. */
    fun loadMore() {
        when (_uiState.value.tab) {
            ShopTab.NEAR -> loadNearShops(reset = false)
            ShopTab.MY -> loadMyShops(reset = false)
            ShopTab.QUICK_ORDER -> Unit
        }
    }

    /** 현재 위치를 다시 읽고 주변 매장을 새로 조회한다(웹 RefreshButton). */
    fun refresh() {
        if (_uiState.value.tab == ShopTab.MY) {
            loadMyShops(reset = true)
        } else {
            resolveLocation()
        }
    }

    fun toggleMyShop(shop: Shop) {
        // 서버 응답을 기다리지 않고 먼저 화면을 바꾼다(실패하면 되돌린다).
        setMyShopFlag(shop.id, !shop.isMyShop)
        viewModelScope.launch {
            runCatching { shopRepository.toggleMyShop(shop.id) }
                .onFailure { setMyShopFlag(shop.id, shop.isMyShop) }
                .onSuccess { if (_uiState.value.tab == ShopTab.MY) loadMyShops(reset = true) }
        }
    }

    /** 매장을 누르면 상세를 불러오고 바텀시트를 연다(웹 ShopBottomSheet). */
    fun openShopDetail(shopId: String) {
        _uiState.update { it.copy(shopDetail = Loadable.Loading) }
        viewModelScope.launch {
            val result = runCatching { shopRepository.getShopDetail(shopId) }
            _uiState.update { state ->
                // 그 사이 시트를 닫았으면 결과를 반영하지 않는다.
                if (state.shopDetail == null) {
                    state
                } else {
                    state.copy(
                        shopDetail = result.fold(
                            onSuccess = { Loadable.Success(it) },
                            onFailure = { Loadable.Failure(it) },
                        ),
                    )
                }
            }
        }
    }

    fun closeShopDetail() {
        _uiState.update { it.copy(shopDetail = null) }
    }

    /** 상세 시트의 즐겨찾기. 목록과 시트 양쪽 상태를 함께 갱신한다. */
    fun toggleMyShopDetail(detail: ShopDetail) {
        val next = !detail.isMyShop
        _uiState.update { it.copy(shopDetail = Loadable.Success(detail.copy(isMyShop = next))) }
        setMyShopFlag(detail.id, next)
        viewModelScope.launch {
            runCatching { shopRepository.toggleMyShop(detail.id) }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(shopDetail = Loadable.Success(detail))
                    }
                    setMyShopFlag(detail.id, detail.isMyShop)
                }
                .onSuccess { if (_uiState.value.tab == ShopTab.MY) loadMyShops(reset = true) }
        }
    }

    private fun resolveLocation() {
        viewModelScope.launch {
            applyLocation(locationProvider.current())
        }
    }

    private fun applyLocation(location: UserLocation) {
        _uiState.update { it.copy(location = location) }
        loadNearShops(reset = true)
    }

    private fun loadNearShops(reset: Boolean) {
        val state = _uiState.value
        val location = state.location ?: return
        loadPage(
            reset = reset,
            current = state.nearShops,
            setState = { list -> _uiState.update { it.copy(nearShops = list) } },
            fetch = { page -> shopRepository.getNearShops(location, page, state.query) },
        )
    }

    private fun loadMyShops(reset: Boolean) {
        if (!_uiState.value.isSignedIn) return
        loadPage(
            reset = reset,
            current = _uiState.value.myShops,
            setState = { list -> _uiState.update { it.copy(myShops = list) } },
            fetch = { page -> shopRepository.getMyShops(page) },
        )
    }

    private fun loadPage(
        reset: Boolean,
        current: ShopListState,
        setState: (ShopListState) -> Unit,
        fetch: suspend (page: Int) -> ShopPage,
    ) {
        if (current.isLoading || current.isLoadingMore) return
        if (!reset && current.isLast) return

        val nextPage = if (reset) 0 else current.shops.size / PAGE_SIZE
        setState(
            current.copy(
                isLoading = reset,
                isLoadingMore = !reset,
                errorMessage = null,
            ),
        )
        viewModelScope.launch {
            runCatching { fetch(nextPage) }
                .onSuccess { page ->
                    val merged = if (reset) page.shops else current.shops + page.shops
                    setState(
                        ShopListState(
                            shops = merged,
                            isLoading = false,
                            isLoadingMore = false,
                            isLast = page.isLast,
                        ),
                    )
                }
                .onFailure { error ->
                    setState(
                        current.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = error.message ?: "매장을 불러오지 못했어요",
                        ),
                    )
                }
        }
    }

    private fun setMyShopFlag(shopId: String, isMyShop: Boolean) {
        _uiState.update { state ->
            state.copy(
                nearShops = state.nearShops.copy(
                    shops = state.nearShops.shops.map {
                        if (it.id == shopId) it.copy(isMyShop = isMyShop) else it
                    },
                ),
                myShops = state.myShops.copy(
                    shops = state.myShops.shops.map {
                        if (it.id == shopId) it.copy(isMyShop = isMyShop) else it
                    },
                ),
            )
        }
    }

    private companion object {
        const val PAGE_SIZE = 10
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
