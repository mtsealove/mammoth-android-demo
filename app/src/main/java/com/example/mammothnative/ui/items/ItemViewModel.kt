package com.example.mammothnative.ui.items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mammothnative.core.Loadable
import com.example.mammothnative.data.remote.TokenStore
import com.example.mammothnative.data.repository.CartRepository
import com.example.mammothnative.data.repository.ItemRepository
import com.example.mammothnative.domain.model.ItemDetail
import com.example.mammothnative.domain.model.OptionGroup
import com.example.mammothnative.domain.model.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MIN_QUANTITY = 1
private const val MAX_QUANTITY = 20

/** 웹에서 옵션을 고르면 200ms 뒤 다음 그룹으로 넘어간다. */
private const val AUTO_STEP_DELAY_MS = 200L

data class ItemUiState(
    val detail: Loadable<ItemDetail> = Loadable.Loading,
    val orderType: OrderType = OrderType.TAKE_OUT,
    /** 옵션 그룹 id → 선택된 옵션 id 들 */
    val selectedOptions: Map<String, Set<String>> = emptyMap(),
    val quantity: Int = MIN_QUANTITY,
    val isAdding: Boolean = false,
    /** 첫 옵션을 고르면 true 가 되고 옵션 패널이 위로 펼쳐진다(웹 `orderBegin`). */
    val orderBegin: Boolean = false,
    /** 펼쳐진 패널에서 지금 펼쳐 보여줄 옵션 그룹 */
    val optionGroupIndex: Int = 0,
    /** 최소 선택을 채우지 않고 '다음'을 눌렀을 때 안내 */
    val showSelectionError: Boolean = false,
    /** 퍼스널 옵션 시트 상태 */
    val isPersonalSheetOpen: Boolean = false,
    val personalGroupIndex: Int = 0,
    /** 적용된 퍼스널 옵션: 그룹 id → (옵션 id → 수량) */
    val personalOptions: Map<String, Map<String, Int>> = emptyMap(),
    /** 시트에서 편집 중인 값. '옵션 적용'을 눌러야 [personalOptions] 로 반영된다. */
    val personalDraft: Map<String, Map<String, Int>> = emptyMap(),
) {
    val item: ItemDetail? get() = detail.dataOrNull

    private val defaultGroups: List<OptionGroup> get() = item?.optionGroups.orEmpty()
    val personalGroups: List<OptionGroup> get() = item?.personalOptionGroups.orEmpty()

    /** 기본 옵션 없이 퍼스널 옵션만 있는 메뉴 (웹 `isOnlyPersonalOption`) */
    val isOnlyPersonalOption: Boolean
        get() = defaultGroups.isEmpty() && personalGroups.isNotEmpty()

    fun isGroupSatisfied(group: OptionGroup): Boolean =
        selectedOptions[group.id].orEmpty().size >= group.minSelection

    /** 기본 옵션을 모두 고른 상태. 이때 주문 버튼이 올라온다(웹 `nextEnabled`). */
    val allDefaultSelected: Boolean
        get() = defaultGroups.all { isGroupSatisfied(it) }

    val isOrderBarVisible: Boolean
        get() = item != null && allDefaultSelected && (orderBegin || defaultGroups.isEmpty())

    /** 퍼스널 옵션 카드는 기본 옵션을 다 고른 뒤에 나타난다. */
    val isPersonalCardVisible: Boolean
        get() = personalGroups.isNotEmpty() && (isOnlyPersonalOption || (allDefaultSelected && orderBegin))

    /** 지금 펼쳐진 그룹의 최소 선택을 못 채우면 '다음'이 비활성 */
    val isNextDisabled: Boolean
        get() {
            val group = defaultGroups.getOrNull(optionGroupIndex)
                ?: defaultGroups.firstOrNull()
                ?: return false
            return !isGroupSatisfied(group)
        }

    val optionPrice: Int
        get() = defaultGroups.sumOf { group ->
            val selected = selectedOptions[group.id].orEmpty()
            group.options.filter { it.id in selected }.sumOf { it.salePrice }
        }

    /** 적용된 퍼스널 옵션 금액 (수량 반영) */
    val personalPrice: Int
        get() = personalOptionPriceOf(personalOptions)

    val personalDraftPrice: Int
        get() = personalOptionPriceOf(personalDraft)

    private fun personalOptionPriceOf(source: Map<String, Map<String, Int>>): Int =
        personalGroups.sumOf { group ->
            val selected = source[group.id].orEmpty()
            group.options.sumOf { option ->
                option.salePrice * (selected[option.id] ?: 0)
            }
        }

    val totalPrice: Int
        get() = ((item?.priceFor(orderType) ?: 0) + optionPrice + personalPrice) * quantity

    val canAddToCart: Boolean
        get() {
            val detail = item ?: return false
            return !detail.isSoldOut && !isAdding && allDefaultSelected
        }

    fun isSelected(groupId: String, optionId: String): Boolean =
        optionId in selectedOptions[groupId].orEmpty()

    /** 접힌 카드 오른쪽에 보여줄 선택값 요약 */
    fun selectedNamesOf(group: OptionGroup): String =
        group.options.filter { isSelected(group.id, it.id) }.joinToString(", ") { it.name }

    fun draftQuantityOf(groupId: String, optionId: String): Int =
        personalDraft[groupId]?.get(optionId) ?: 0

    /** 적용된 퍼스널 옵션 목록 (카드에 요약으로 보여준다) */
    fun appliedPersonalOptions(): List<Triple<String, Int, Int>> = personalGroups.flatMap { group ->
        val selected = personalOptions[group.id].orEmpty()
        group.options.mapNotNull { option ->
            val quantity = selected[option.id] ?: return@mapNotNull null
            Triple(option.name, quantity, option.salePrice * quantity)
        }
    }
}

sealed interface ItemEvent {
    data object AddedToCart : ItemEvent
    data class Message(val text: String) : ItemEvent
    data object RequireSignIn : ItemEvent
}

/**
 * 웹 ItemActivity 의 상태.
 *
 * 옵션은 웹과 같은 단계식이다: 처음에는 첫 옵션 그룹만 보이고, 고르면 패널이 올라오면서
 * 다음 그룹으로 이동한다. 기본 옵션을 다 고르면 주문 버튼과 퍼스널 옵션 카드가 나타난다.
 */
@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val cartRepository: CartRepository,
    private val tokenStore: TokenStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"])
    private val shopId: String = checkNotNull(savedStateHandle["shopId"])
    private val orderType: OrderType = OrderType.from(savedStateHandle["orderType"])

    private val _uiState = MutableStateFlow(ItemUiState(orderType = orderType))
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    private val _events = Channel<ItemEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun onOptionClick(groupId: String, optionId: String) {
        val state = _uiState.value
        val group = state.item?.optionGroups?.firstOrNull { it.id == groupId } ?: return
        val current = state.selectedOptions[groupId].orEmpty()
        val wasSelected = optionId in current

        val next = when {
            group.isSingleSelect -> setOf(optionId)
            wasSelected -> current - optionId
            current.size >= group.maxSelection -> current
            else -> current + optionId
        }
        _uiState.update {
            it.copy(
                selectedOptions = it.selectedOptions + (groupId to next),
                showSelectionError = false,
            )
        }

        // 새로 고른 경우에만 다음 그룹으로 넘어간다(해제는 제외).
        if (!wasSelected) {
            viewModelScope.launch {
                delay(AUTO_STEP_DELAY_MS)
                stepToNextGroup()
            }
        }
    }

    /** '다음' 버튼 */
    fun onNextClick() {
        if (_uiState.value.isNextDisabled) {
            _uiState.update { it.copy(showSelectionError = true) }
            return
        }
        stepToNextGroup()
    }

    /** 접힌 옵션 카드를 눌러 펼치기 */
    fun onOptionCardClick(index: Int) {
        _uiState.update { it.copy(optionGroupIndex = index, showSelectionError = false) }
    }

    fun onQuantityChange(delta: Int) {
        _uiState.update {
            it.copy(quantity = (it.quantity + delta).coerceIn(MIN_QUANTITY, MAX_QUANTITY))
        }
    }

    // --- 퍼스널 옵션 ---

    fun openPersonalSheet() {
        _uiState.update {
            it.copy(
                isPersonalSheetOpen = true,
                // 편집은 사본에서 하고 '옵션 적용' 때 반영한다.
                personalDraft = it.personalOptions,
                personalGroupIndex = 0,
            )
        }
    }

    fun closePersonalSheet() {
        _uiState.update { it.copy(isPersonalSheetOpen = false) }
    }

    fun onPersonalGroupSelect(index: Int) {
        _uiState.update { it.copy(personalGroupIndex = index) }
    }

    fun onPersonalOptionClick(groupId: String, optionId: String) {
        val state = _uiState.value
        val group = state.personalGroups.firstOrNull { it.id == groupId } ?: return
        val current = state.personalDraft[groupId].orEmpty()
        val selected = current.containsKey(optionId)

        val next = when {
            group.isSingleSelect -> if (selected) emptyMap() else mapOf(optionId to 1)
            selected -> current - optionId
            current.size >= group.maxSelection -> current
            else -> current + (optionId to 1)
        }
        _uiState.update { it.copy(personalDraft = it.personalDraft + (groupId to next)) }
    }

    fun onPersonalQuantityChange(groupId: String, optionId: String, delta: Int) {
        val state = _uiState.value
        val group = state.personalGroups.firstOrNull { it.id == groupId } ?: return
        val option = group.options.firstOrNull { it.id == optionId } ?: return
        val current = state.personalDraft[groupId].orEmpty()
        val quantity = (current[optionId] ?: 0) + delta

        val next = if (quantity <= 0) {
            current - optionId
        } else {
            current + (optionId to quantity.coerceAtMost(option.maxQuantity))
        }
        _uiState.update { it.copy(personalDraft = it.personalDraft + (groupId to next)) }
    }

    /** 시트의 '옵션 적용' (웹 flushOptions) */
    fun applyPersonalOptions() {
        _uiState.update {
            it.copy(
                personalOptions = it.personalDraft.filterValues { options -> options.isNotEmpty() },
                isPersonalSheetOpen = false,
            )
        }
    }

    fun addToCart() {
        val state = _uiState.value
        val detail = state.item ?: return
        if (!state.canAddToCart) return

        viewModelScope.launch {
            if (!tokenStore.isSignedIn.first()) {
                _events.send(ItemEvent.RequireSignIn)
                return@launch
            }
            _uiState.update { it.copy(isAdding = true) }
            val options = buildList {
                detail.optionGroups.forEach { group ->
                    state.selectedOptions[group.id].orEmpty().forEach { optionId ->
                        add(optionId to 1)
                    }
                }
                detail.personalOptionGroups.forEach { group ->
                    state.personalOptions[group.id].orEmpty().forEach { (optionId, quantity) ->
                        add(optionId to quantity)
                    }
                }
            }
            runCatching {
                cartRepository.addToCart(
                    shopId = shopId,
                    itemId = detail.id,
                    quantity = state.quantity,
                    options = options,
                )
            }
                .onSuccess {
                    _uiState.update { it.copy(isAdding = false) }
                    _events.send(ItemEvent.AddedToCart)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isAdding = false) }
                    _events.send(ItemEvent.Message(error.message ?: "장바구니에 담지 못했어요"))
                }
        }
    }

    /** 최소 선택을 채우지 못한 첫 그룹으로 이동한다(웹 handleNext). */
    private fun stepToNextGroup() {
        _uiState.update { state ->
            val groups = state.item?.optionGroups.orEmpty()
            val nextIndex = groups.indexOfFirst { !state.isGroupSatisfied(it) }
            state.copy(
                orderBegin = true,
                // 다 골랐으면 마지막 그룹에 머무른다.
                optionGroupIndex = if (nextIndex >= 0) nextIndex else groups.lastIndex.coerceAtLeast(0),
                showSelectionError = false,
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(detail = Loadable.Loading) }
            runCatching { itemRepository.getItem(itemId) }
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            detail = Loadable.Success(detail),
                            // 웹 useInitOrder 와 같이 기본 옵션을 미리 선택해 둔다.
                            selectedOptions = detail.optionGroups.associate { group ->
                                group.id to group.options
                                    .filter { option -> option.isDefault }
                                    .map { option -> option.id }
                                    .take(group.maxSelection)
                                    .toSet()
                            },
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(detail = Loadable.Failure(error)) } }
        }
    }
}
