package com.example.mammothnative.domain.model

data class Shop(
    val id: String,
    val name: String,
    val address: String,
    val thumbnailUrl: String?,
    val distanceText: String?,
    val badges: List<String>,
    val orderStatus: ShopOrderStatus,
    val isMyShop: Boolean,
    val isTestShop: Boolean,
) {
    /**
     * 웹 ShopItem 의 도보 시간 계산(분속 67m 기준).
     * 서버가 "350 m" / "1.2 km" 형태로 내려주므로 숫자만 뽑아 쓴다.
     */
    val walkMinutes: Int?
        get() {
            val match = DISTANCE_REGEX.find(distanceText?.trim().orEmpty()) ?: return null
            val (value, unit) = match.destructured
            val meters = value.toDoubleOrNull()?.let {
                if (unit.lowercase() == "km") it * 1000 else it
            } ?: return null
            return (meters / WALK_METER_PER_MINUTE).roundToIntAtLeastOne()
        }

    private fun Double.roundToIntAtLeastOne() = maxOf(Math.round(this).toInt(), 1)

    private companion object {
        val DISTANCE_REGEX = Regex("""^([\d.]+)\s*(m|km)$""", RegexOption.IGNORE_CASE)
        const val WALK_METER_PER_MINUTE = 67
    }
}

/** 웹 `ShopOrderStatus` + `ShopOrderStatusKr` */
enum class ShopOrderStatus(val label: String) {
    OPEN("영업중"),
    CLOSED("영업 종료"),
    BREAK_TIME("준비중"),
    UNKNOWN("");

    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}

/** 한 페이지 조회 결과 */
data class ShopPage(
    val shops: List<Shop>,
    val isLast: Boolean,
    val currentPage: Int,
)

/** 매장 상세 (GET /v0/shop/{shopId}) */
data class ShopDetail(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String?,
    val openHours: String?,
    val orderStatus: ShopOrderStatus,
    val isMyShop: Boolean,
    val latitude: Double,
    val longitude: Double,
    val kindName: String?,
    val kindNote: String?,
    /** 매장 식사 가능 여부 */
    val useHall: Boolean,
    /** 포장 가능 여부 */
    val useTakeout: Boolean,
) {
    /** 웹 OrderTypeSelector: 영업중이 아니면 주문 방식 선택이 막힌다. */
    val orderEnabled: Boolean get() = orderStatus == ShopOrderStatus.OPEN
}
