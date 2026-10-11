package com.example.mammothnative.data.remote.interceptor

import com.example.mammothnative.data.remote.TokenStore
import com.example.mammothnative.data.remote.api.AuthApi
import com.example.mammothnative.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * 401 을 받으면 리프레시 토큰으로 재발급하고 원래 요청을 한 번 더 보낸다.
 * 웹 instance.ts 의 401 인터셉터와 같은 역할이며, 서버가 리프레시 토큰을 회전시키므로
 * 동시에 터진 401 들이 재발급을 각각 일으키지 않도록 Mutex 로 한 번만 수행한다.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    // AuthApi 는 이 Authenticator 가 붙지 않은 클라이언트로 만들어진다(재귀 방지).
    private val authApi: Provider<AuthApi>,
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // 재시도한 요청이 또 401 이면 포기한다.
        if (response.priorResponse != null) return null

        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")

        return runBlocking {
            mutex.withLock {
                val current = tokenStore.currentAccessToken()
                // 다른 요청이 이미 재발급을 끝냈다면 새 토큰으로 바로 재시도한다.
                if (!current.isNullOrBlank() && current != failedToken) {
                    return@withLock response.request.withToken(current)
                }
                val refreshToken = tokenStore.currentRefreshToken() ?: return@withLock null
                val reissued = runCatching {
                    authApi.get().refresh(RefreshTokenRequest(refreshToken))
                }.getOrElse {
                    // 세션이 끝난 것으로 보고 토큰을 비운다. 화면은 로그아웃 상태로 전환된다.
                    tokenStore.clear()
                    return@withLock null
                }
                tokenStore.save(reissued.token, reissued.refreshToken)
                response.request.withToken(reissued.token)
            }
        }
    }

    private fun Request.withToken(token: String): Request = newBuilder()
        .header("Authorization", "Bearer $token")
        .build()
}
