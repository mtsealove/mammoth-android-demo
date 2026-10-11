package com.example.mammothnative.ui.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.data.repository.CartRepository
import com.example.mammothnative.data.repository.ItemRepository
import com.example.mammothnative.data.repository.ShopRepository
import com.example.mammothnative.domain.model.ItemCategory
import com.example.mammothnative.domain.model.MenuItem
import com.example.mammothnative.domain.model.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemsUiState(
    val shopName: String = "",
    val orderType: OrderType = OrderType.TAKE_OUT,
    val categories: Loadable<List<ItemCategory>> = Loadable.Loading,
    val selectedCategoryIndex: Int = 0,
    val selectedSubCategoryIndex: Int = 0,
    val items: Loadable<List<MenuItem>> = Loadable.Loading,
    val cartCount: Int = 0,
) {
    val selectedCategory: ItemCategory?
        get() = categories.dataOrNull?.getOrNull(selectedCategoryIndex)
}

/** 웹 ItemsActivity: 매장의 카테고리 탭 + 메뉴 목록. */
@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val shopRepository: ShopRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val shopId: String = checkNotNull(savedStateHandle["shopId"])
    private val orderType: OrderType = OrderType.from(savedStateHandle["orderType"])

    private val _uiState = MutableStateFlow(ItemsUiState(orderType = orderType))
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()

    init {
        loadShopName()
        loadCategories()
        refreshCartCount()
    }

    fun onCategorySelect(index: Int) {
        if (_uiState.value.selectedCategoryIndex == index) return
        _uiState.update {
            it.copy(selectedCategoryIndex = index, selectedSubCategoryIndex = 0)
        }
        loadItems()
    }

    fun onSubCategorySelect(index: Int) {
        if (_uiState.value.selectedSubCategoryIndex == index) return
        _uiState.update { it.copy(selectedSubCategoryIndex = index) }
        loadItems()
    }

    /** 메뉴를 담고 돌아왔을 때 장바구니 배지를 다시 맞춘다. */
    fun refreshCartCount() {
        viewModelScope.launch {
            runCatching { cartRepository.getCartCount(shopId) }
                .onSuccess { count -> _uiState.update { it.copy(cartCount = count) } }
        }
    }

    private fun loadShopName() {
        viewModelScope.launch {
            runCatching { shopRepository.getShopDetail(shopId) }
                .onSuccess { shop -> _uiState.update { it.copy(shopName = shop.name) } }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(categories = Loadable.Loading) }
            runCatching { itemRepository.getCategories(shopId) }
                .onSuccess { categories ->
                    _uiState.update { it.copy(categories = Loadable.Success(categories)) }
                    loadItems()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(categories = Loadable.Failure(error)) }
                }
        }
    }

    private fun loadItems() {
        val state = _uiState.value
        val category = state.selectedCategory ?: return
        // 하위 카테고리를 쓰는 묶음은 선택된 하위 카테고리의 id 로 조회한다.
        val categoryId = if (category.usesSubCategory) {
            category.children.getOrNull(state.selectedSubCategoryIndex)?.id ?: return
        } else {
            category.id
        }

        viewModelScope.launch {
            _uiState.update { it.copy(items = Loadable.Loading) }
            runCatching { itemRepository.getItems(shopId, categoryId) }
                .onSuccess { items -> _uiState.update { it.copy(items = Loadable.Success(items)) } }
                .onFailure { error -> _uiState.update { it.copy(items = Loadable.Failure(error)) } }
        }
    }
}
