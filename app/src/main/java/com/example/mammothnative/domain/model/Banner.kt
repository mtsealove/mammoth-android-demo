package com.example.mammothnative.domain.model

data class Banner(
    val id: String,
    val imageUrl: String?,
    val linkType: BannerLinkType,
    val linkUrl: String?,
    val contentType: String?,
    val contentId: String?,
)

enum class BannerLinkType {
    CONTENT_DETAIL, LINK, NONE, STAMP, COUPON_BOX, GIFT, PREPAID_CARD;

    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.name == raw } ?: NONE
    }
}

/** 웹 `BannerType`. 지금은 메인 세로 배너만 쓴다. */
enum class BannerType { MAIN_VERTICAL, POPUP, GIFT, ALL_MENU }
