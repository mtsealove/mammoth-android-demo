package com.example.mammothnative.domain.model

/** 웹 `UserProfileResponse` 중 앱에서 실제로 쓰는 항목. */
data class UserProfile(
    val id: String,
    val nickname: String,
    val phoneNumber: String?,
    val barcodeNo: String?,
    val couponCount: Int,
    val points: Int,
    val stampCount: Int,
)
