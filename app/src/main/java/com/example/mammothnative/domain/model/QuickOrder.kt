package com.example.mammothnative.domain.model

/** 홈 퀵메뉴의 "최근 주문" 카드 (GET /v1/orders/recent) */
data class RecentOrder(
    val orderId: String,
    val orderName: String,
    val shopName: String,
    val paymentAmount: Int,
)

/** 홈 퀵메뉴의 "MY메뉴" 카드 (GET /v0/mymenus) */
data class MyMenu(
    val myMenuId: String,
    val name: String,
    val shopName: String,
    val itemName: String,
    val price: Int,
    val thumbnailUrl: String?,
)
