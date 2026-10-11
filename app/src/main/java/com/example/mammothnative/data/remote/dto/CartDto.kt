package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** GET /v0/shop/{shopId}/cart */
@Serializable
data class CartIdDto(
    val cartId: String? = null,
)

/** GET /v0/shop/{shopId}/cart/{cartId} */
@Serializable
data class CartDto(
    val items: List<CartItemDto> = emptyList(),
)

@Serializable
data class CartItemDto(
    val cartItemId: String? = null,
    val itemId: String? = null,
    val name: String? = null,
    val qty: Int = 0,
)

/** POST /v0/shop/{shopId}/cart/item */
@Serializable
data class AddToCartRequest(
    val projectId: String,
    val itemId: String,
    val quantity: Int,
    val options: List<CartOptionRequest> = emptyList(),
)

@Serializable
data class CartOptionRequest(
    val optionId: String,
    val quantity: Int = 1,
)
