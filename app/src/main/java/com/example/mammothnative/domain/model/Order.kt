package com.example.mammothnative.domain.model

data class ActiveOrder(
    val orderNo: String,
    val shopName: String,
    val displayOrderNo: String,
    val status: OrderStatus,
    val thumbnailUrl: String?,
) {
    /** 웹 `useStatusMessage` */
    val statusMessage: String
        get() {
            val no = displayOrderNo.padStart(4, '0')
            return when (status) {
                OrderStatus.NEW -> "${no}번 주문을 확인 중이에요."
                OrderStatus.PREPARING -> "${no}번 주문을 준비 중이에요."
                OrderStatus.READY -> "${no}번 주문이 준비되었어요."
                OrderStatus.DONE -> "${no}번 주문이 완료되었어요."
                OrderStatus.UNKNOWN -> ""
            }
        }
}

enum class OrderStatus {
    NEW, PREPARING, READY, DONE, UNKNOWN;

    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}
