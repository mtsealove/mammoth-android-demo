package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** GET /v0/mymenus */
@Serializable
data class MyMenuListDto(
    val myMenus: List<MyMenuDto> = emptyList(),
)

@Serializable
data class MyMenuDto(
    val myMenuId: String,
    val myMenuName: String? = null,
    val shopName: String? = null,
    val item: MyMenuItemDto? = null,
)

@Serializable
data class MyMenuItemDto(
    val itemName: String? = null,
    val salePrice: Double = 0.0,
    val thumbnailUrl: String? = null,
    val qty: Int = 1,
)
