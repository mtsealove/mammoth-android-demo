package com.example.mammothnative.domain.model

/** 주문 방식. 웹 `OrderType` */
enum class OrderType(val label: String) {
    IN_SHOP("매장"),
    TAKE_OUT("포장");

    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.name == raw } ?: TAKE_OUT
    }
}

data class ItemCategory(
    val id: String,
    val name: String,
    val usesSubCategory: Boolean,
    val children: List<SubCategory>,
)

data class SubCategory(
    val id: String,
    val name: String,
)

/** 메뉴 목록의 한 줄 */
data class MenuItem(
    val id: String,
    val name: String,
    val description: String?,
    val thumbnailUrl: String?,
    val salePrice: Int,
    val inShopPrice: Int?,
    val badges: List<String>,
    val isSoldOut: Boolean,
) {
    /** 웹 useResolvedSalePrice: 매장 주문이면 매장가가 있을 때 그 값을 쓴다. */
    fun priceFor(orderType: OrderType): Int =
        if (orderType == OrderType.IN_SHOP) inShopPrice ?: salePrice else salePrice
}

/** 메뉴 상세 */
data class ItemDetail(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val salePrice: Int,
    val inShopPrice: Int?,
    val isSoldOut: Boolean,
    val warnings: String?,
    /** 기본 옵션(온도·사이즈 등). 주문 전에 반드시 고르는 값들이다. */
    val optionGroups: List<OptionGroup>,
    /** 퍼스널 옵션(샷 추가 등). 별도 시트에서 고른다. */
    val personalOptionGroups: List<OptionGroup>,
) {
    fun priceFor(orderType: OrderType): Int =
        if (orderType == OrderType.IN_SHOP) inShopPrice ?: salePrice else salePrice
}

data class OptionGroup(
    val id: String,
    val name: String,
    /** RADIO 는 하나만, CHECKBOX 는 maxSelection 까지 고를 수 있다. */
    val isSingleSelect: Boolean,
    val minSelection: Int,
    val maxSelection: Int,
    val options: List<ItemOption>,
) {
    val isRequired: Boolean get() = minSelection > 0
}

data class ItemOption(
    val id: String,
    val name: String,
    val salePrice: Int,
    val isDefault: Boolean,
    val isChangeable: Boolean,
    /** 1보다 크면 수량 조절이 가능한 옵션이다(샷 추가 등). */
    val maxQuantity: Int = 1,
)
