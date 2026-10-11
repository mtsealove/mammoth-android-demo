package com.example.mammothnative.data.repository

import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.domain.model.ItemCategory
import com.example.mammothnative.domain.model.ItemDetail
import com.example.mammothnative.domain.model.ItemOption
import com.example.mammothnative.domain.model.MenuItem
import com.example.mammothnative.domain.model.OptionGroup
import com.example.mammothnative.domain.model.SubCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val api: MammothApi,
) {
    suspend fun getCategories(shopId: String): List<ItemCategory> =
        api.getItemCategories(shopId).categories.map { dto ->
            ItemCategory(
                id = dto.id,
                name = dto.name.orEmpty(),
                usesSubCategory = dto.usesSubCategory,
                children = dto.children.map { SubCategory(it.id, it.name.orEmpty()) },
            )
        }

    suspend fun getItems(shopId: String, categoryId: String): List<MenuItem> =
        api.getItems(shopId, categoryId).items.map { dto ->
            MenuItem(
                id = dto.id,
                name = dto.name.orEmpty(),
                description = dto.description?.takeIf(String::isNotBlank),
                thumbnailUrl = dto.thumbnailUrl?.takeIf(String::isNotBlank),
                salePrice = dto.salePrice.toWon(),
                inShopPrice = dto.inShopPrice?.toWon(),
                badges = dto.badges,
                isSoldOut = dto.isSoldOut,
            )
        }

    suspend fun getItem(itemId: String): ItemDetail = api.getItem(itemId).let { dto ->
        ItemDetail(
            id = dto.id,
            name = dto.name.orEmpty(),
            description = dto.description?.takeIf(String::isNotBlank),
            imageUrl = dto.imageUrls.firstOrNull() ?: dto.thumbnailImage,
            salePrice = dto.salePrice.toWon(),
            inShopPrice = dto.inShopPrice?.toWon(),
            isSoldOut = dto.isSoldOut,
            warnings = dto.warnings?.takeIf(String::isNotBlank),
            optionGroups = dto.defaultOptionGroups.map { it.toDomain() },
            personalOptionGroups = dto.personalOptionGroups.map { it.toDomain() },
        )
    }
}

private fun com.example.mammothnative.data.remote.dto.OptionGroupDto.toDomain() = OptionGroup(
    id = optionGroupId,
    name = name.orEmpty(),
    isSingleSelect = uiButtonType != "CHECKBOX",
    minSelection = minSelection,
    maxSelection = maxSelection.coerceAtLeast(1),
    options = options.map {
        ItemOption(
            id = it.optionId,
            name = it.name.orEmpty(),
            salePrice = it.salePrice.toWon(),
            isDefault = it.isDefault,
            isChangeable = it.isChangeable,
            maxQuantity = it.maxQuantity.coerceAtLeast(1),
        )
    },
)

/** 소수로 내려오는 금액을 원 단위 정수로 바꾼다. */
private fun Double.toWon(): Int = Math.round(this).toInt()
