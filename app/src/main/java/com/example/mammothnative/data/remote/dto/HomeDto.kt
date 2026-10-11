package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** GET /v0/home — 웹 `models/home/response.ts` 의 HomeResponse */
@Serializable
data class HomeSettingsDto(
    val bgSize: String? = null,
    val bgType: String? = null,
    val bgColor: String? = null,
    val bgImageUrl: String? = null,
    val conditionalCopyMode: String? = null,
    val quickMenuExpanded: Boolean = false,
    val quickMenuItems: List<QuickMenuItemDto> = emptyList(),
)

@Serializable
data class QuickMenuItemDto(
    val menuType: String,
    val name: String? = null,
    val iconUrl: String? = null,
    val targetId: String? = null,
    val tooltip: String? = null,
)

/** GET /v0/conditional-copy */
@Serializable
data class ConditionalCopyDto(
    val message: String? = null,
    val menuType: String? = null,
)
