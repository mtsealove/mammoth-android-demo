package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** GET /v0/orders/active */
@Serializable
data class ActiveOrderListDto(
    val orders: List<ActiveOrderDto> = emptyList(),
)

@Serializable
data class ActiveOrderDto(
    val orderNo: String,
    val shopName: String? = null,
    val shopOrderNo: Int? = null,
    val status: String? = null,
    val statusUpdatedAt: String? = null,
    val displayOrderNo: String? = null,
    val thumbNailImageUrl: String? = null,
)

/** GET /v1/orders/recent */
@Serializable
data class RecentOrderListDto(
    val orders: List<RecentOrderDto> = emptyList(),
)

@Serializable
data class RecentOrderDto(
    val orderId: String,
    val orderNo: String? = null,
    val shopName: String? = null,
    val orderName: String? = null,
    val paymentAmount: Double = 0.0,
    val createdAt: String? = null,
)
