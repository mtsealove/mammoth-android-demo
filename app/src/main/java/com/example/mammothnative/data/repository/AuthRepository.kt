package com.example.mammothnative.data.repository

import com.example.mammothnative.BuildConfig
import com.example.mammothnative.data.remote.TokenStore
import com.example.mammothnative.data.remote.api.AccountApi
import com.example.mammothnative.data.remote.api.AuthApi
import com.example.mammothnative.data.remote.dto.RefreshTokenRequest
import com.example.mammothnative.data.remote.dto.SignInRequest
import com.example.mammothnative.data.remote.dto.SmsRequest
import com.example.mammothnative.data.remote.dto.SmsVerifyRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 웹 SignInWithPhoneActivity 의 흐름을 그대로 옮겼다.
 * 인증번호 발송 → 확인 → 로그인(토큰 저장) 순서다.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val accountApi: AccountApi,
    private val tokenStore: TokenStore,
) {
    val isSignedIn: Flow<Boolean> = tokenStore.isSignedIn

    suspend fun sendSms(phoneNo: String) {
        authApi.sendSms(SmsRequest(projectId = BuildConfig.PROJECT_ID, phoneNo = phoneNo))
    }

    suspend fun verifySms(phoneNo: String, code: String) {
        authApi.verifySms(
            SmsVerifyRequest(
                projectId = BuildConfig.PROJECT_ID,
                phoneNo = phoneNo,
                smsAuthNo = code,
            ),
        )
    }

    /** 인증을 마친 번호로 로그인하고 토큰을 저장한다. */
    suspend fun signInWithPhone(phoneNo: String) {
        // 옛 토큰이 남아 있으면 재발급이 끼어들 수 있어 먼저 비운다(웹과 동일).
        tokenStore.clear()
        val token = authApi.signInWithPhone(
            SignInRequest(projectId = BuildConfig.PROJECT_ID, phoneNo = phoneNo),
        )
        tokenStore.save(token.token, token.refreshToken)
    }

    /** 서버 로그아웃이 실패하더라도 기기의 토큰은 반드시 지운다. */
    suspend fun signOut() {
        val refreshToken = tokenStore.currentRefreshToken()
        if (refreshToken != null) {
            runCatching { accountApi.signOut(RefreshTokenRequest(refreshToken)) }
        }
        tokenStore.clear()
    }
}
