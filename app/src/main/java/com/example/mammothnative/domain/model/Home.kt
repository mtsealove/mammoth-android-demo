package com.example.mammothnative.domain.model

/** 웹 `models/home` 대응 도메인 모델. */
data class HomeSettings(
    val backgroundSize: HomeBackgroundSize,
    val backgroundType: HomeBackgroundType,
    val backgroundColor: String?,
    val backgroundImageUrl: String?,
    val quickMenuExpanded: Boolean,
    val quickMenuItems: List<QuickMenuItem>,
)

enum class HomeBackgroundSize { SMALL, LARGE;
    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.name == raw } ?: SMALL
    }
}

enum class HomeBackgroundType { COLOR, IMAGE;
    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.name == raw } ?: COLOR
    }
}

data class QuickMenuItem(
    val type: QuickMenuType,
    val name: String,
    val iconUrl: String?,
    val targetId: String?,
)

/**
 * 웹 `HomeMenuType` + `HomeQuickMenuTypeKr`.
 * 서버가 새 타입을 추가해도 앱이 죽지 않도록 [UNKNOWN] 으로 떨어뜨린다.
 */
enum class QuickMenuType(val label: String, val authRequired: Boolean) {
    PREPAID_CARD("선불카드", true),
    GIFT("선물하기", true),
    EVENT("이벤트", true),
    NOTICE("공지사항", false),
    REWARD("혜택", true),
    COUPON("쿠폰", true),
    ORDER("주문하기", false),
    ORDER_HISTORY("주문 내역", true),
    MY_MENU("마이메뉴", true),
    FREQUENCY("프리퀀시", true),
    ROULETTE_EVENT("룰렛 이벤트", true),
    UNKNOWN("", false);

    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}

/** 웹 `conditional-copy` 응답. 문구와, 눌렀을 때 이동할 메뉴를 함께 받는다. */
data class ConditionalCopy(
    val message: String?,
    val menuType: QuickMenuType?,
) {
    /** 웹 `ConditionalButtonLabel` */
    val buttonLabel: String?
        get() = when (menuType) {
            null, QuickMenuType.UNKNOWN -> null
            QuickMenuType.ORDER -> "주문하기"
            else -> "바로가기"
        }
}
