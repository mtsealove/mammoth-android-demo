package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** GET /v1/shop/{shopId}/categories */
/**
 * 서버는 금액을 `2300.00` 처럼 소수로 내려준다. DTO 에서는 Double 로 받고
 * 도메인 모델로 옮길 때 원 단위 정수로 바꾼다.
 */
@Serializable
data class ItemCategoriesDto(
    val categories: List<ItemCategoryDto> = emptyList(),
)

@Serializable
data class ItemCategoryDto(
    val id: String,
    val name: String? = null,
    val usesSubCategory: Boolean = false,
    val children: List<SubCategoryDto> = emptyList(),
)

@Serializable
data class SubCategoryDto(
    val id: String,
    val name: String? = null,
)

/** GET /v1/shop/{shopId}/items?categoryId= */
@Serializable
data class ItemsDto(
    val items: List<ItemRowDto> = emptyList(),
)

@Serializable
data class ItemRowDto(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val salePrice: Double = 0.0,
    val inShopPrice: Double? = null,
    val badges: List<String> = emptyList(),
    val isSoldOut: Boolean = false,
)

/** GET /v1/item/{itemId} */
@Serializable
data class ItemDetailDto(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val imageUrls: List<String> = emptyList(),
    val thumbnailImage: String? = null,
    val salePrice: Double = 0.0,
    val inShopPrice: Double? = null,
    val isSoldOut: Boolean = false,
    val warnings: String? = null,
    val defaultOptionGroups: List<OptionGroupDto> = emptyList(),
    val personalOptionGroups: List<OptionGroupDto> = emptyList(),
)

@Serializable
data class OptionGroupDto(
    val optionGroupId: String,
    val name: String? = null,
    /** RADIO | CHECKBOX */
    val uiButtonType: String? = null,
    val minSelection: Int = 0,
    val maxSelection: Int = 1,
    val options: List<ItemOptionDto> = emptyList(),
)

@Serializable
data class ItemOptionDto(
    val optionId: String,
    val name: String? = null,
    val salePrice: Double = 0.0,
    val isDefault: Boolean = false,
    val isChangeable: Boolean = true,
    val maxQuantity: Int = 1,
)
