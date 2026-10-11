package com.example.mammothnative.domain.model

data class StampCard(
    val shopGroupId: String?,
    val totalSlots: Int,
    val stampCount: Int,
) {
    val progress: Float
        get() = if (totalSlots <= 0) 0f else (stampCount.toFloat() / totalSlots).coerceIn(0f, 1f)

    /** 웹 ExpressStamp 의 안내 문구 */
    val message: String
        get() = if (stampCount == 0) {
            "${totalSlots}개 적립 시 리워드 쿠폰을 드려요!"
        } else {
            "리워드 쿠폰까지 ${(totalSlots - stampCount).coerceAtLeast(0)}개"
        }
}
