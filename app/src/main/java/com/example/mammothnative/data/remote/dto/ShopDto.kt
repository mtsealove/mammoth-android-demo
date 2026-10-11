package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** 웹 `models/pageable` 의 SliceResponse */
@Serializable
data class SliceDto<T>(
    val content: List<T> = emptyList(),
    val slice: SliceInfoDto = SliceInfoDto(),
)

@Serializable
data class SliceInfoDto(
    val currentPage: Int = 0,
    val size: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val empty: Boolean = true,
)

/** GET /v0/shops */
@Serializable
data class ShopDto(
    val id: String,
    val name: String? = null,
    val address: String? = null,
    val thumbnailImageUrl: String? = null,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    /** "350 m" 처럼 서버가 포맷해서 내려준다. */
    val distanceFromUser: String? = null,
    val badges: List<String> = emptyList(),
    val orderStatus: String? = null,
    val isMyShop: Boolean = false,
    val isTestShop: Boolean = false,
)

/** GET /v0/shop/{shopId} */
@Serializable
data class ShopDetailDto(
    val id: String,
    val name: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val openhours: String? = null,
    val images: List<String> = emptyList(),
    val orderStatus: String? = null,
    val isMyShop: Boolean = false,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val shopKindInfo: ShopKindDto? = null,
)

@Serializable
data class ShopKindDto(
    val id: String? = null,
    val name: String? = null,
    val note: String? = null,
    val diningOption: DiningOptionDto? = null,
)

@Serializable
data class DiningOptionDto(
    val useHall: Boolean = true,
    val useTakeout: Boolean = true,
)
