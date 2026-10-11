package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** GET /v0/banners?bannerType= */
@Serializable
data class BannerListDto(
    val banners: List<BannerDto> = emptyList(),
)

@Serializable
data class BannerDto(
    val id: String,
    val imageUrl: String? = null,
    val contentType: String? = null,
    val contentId: String? = null,
    val bannerLinkType: String? = null,
    val bannerLinkUrl: String? = null,
    val eventType: String? = null,
)
