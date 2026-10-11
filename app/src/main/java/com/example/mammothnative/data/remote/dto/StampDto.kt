package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** GET /v0/stamp-cards */
@Serializable
data class StampCardListDto(
    val cards: List<StampCardDto> = emptyList(),
)

@Serializable
data class StampCardDto(
    val shopGroupId: String? = null,
    val shopGroupName: String? = null,
    val totalSlots: Int = 0,
    val stampCount: Int = 0,
)
