package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** POST /v0/auth/token/refresh */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class TokenDto(
    val token: String,
    val refreshToken: String,
)

/** 서버 공통 오류 본문 */
@Serializable
data class ErrorDto(
    val code: String? = null,
    val message: String? = null,
)

/** POST /v0/auth/sms */
@Serializable
data class SmsRequest(
    val projectId: String,
    val phoneNo: String,
)

/** POST /v0/auth/sms/verify */
@Serializable
data class SmsVerifyRequest(
    val projectId: String,
    val phoneNo: String,
    val smsAuthNo: String,
)

/** POST /v0/auth/signin */
@Serializable
data class SignInRequest(
    val projectId: String,
    val phoneNo: String,
)

/** GET /v0/user */
@Serializable
data class UserProfileDto(
    val id: String,
    val nickname: String? = null,
    val phoneNumber: String? = null,
    val barcodeNo: String? = null,
    val couponCount: Int = 0,
    val points: Int? = null,
    val stampCount: Int? = null,
)
