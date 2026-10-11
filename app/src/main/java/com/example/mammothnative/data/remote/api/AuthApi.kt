package com.example.mammothnative.data.remote.api

import com.example.mammothnative.data.remote.dto.RefreshTokenRequest
import com.example.mammothnative.data.remote.dto.SignInRequest
import com.example.mammothnative.data.remote.dto.SmsRequest
import com.example.mammothnative.data.remote.dto.SmsVerifyRequest
import com.example.mammothnative.data.remote.dto.TokenDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 토큰이 필요 없는 인증 엔드포인트.
 *
 * 인증 인터셉터·401 재발급이 붙지 않은 OkHttpClient 로 만든다.
 * 웹에서도 같은 이유로 로그인/재발급 요청만 bare axios 를 썼다
 * (남아 있던 옛 refreshToken 때문에 로그인 흐름이 세션 만료 처리로 끌려가는 것을 막는다).
 */
interface AuthApi {
    @POST("v0/auth/token/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): TokenDto

    /** 인증번호 발송 */
    @POST("v0/auth/sms")
    suspend fun sendSms(@Body body: SmsRequest)

    /** 인증번호 확인 */
    @POST("v0/auth/sms/verify")
    suspend fun verifySms(@Body body: SmsVerifyRequest)

    /** 휴대폰 번호 로그인. 가입되지 않은 번호면 실패한다(웹은 이때 회원가입으로 보낸다). */
    @POST("v0/auth/signin")
    suspend fun signInWithPhone(@Body body: SignInRequest): TokenDto
}
