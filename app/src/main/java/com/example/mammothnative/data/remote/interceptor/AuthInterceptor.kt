package com.example.mammothnative.data.remote.interceptor

import com.example.mammothnative.data.remote.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** 저장된 액세스 토큰이 있으면 Authorization 헤더를 붙인다. */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // OkHttp 인터셉터는 동기 API 라 여기서만 블로킹한다(DataStore 읽기 한 번).
        val token = runBlocking { tokenStore.currentAccessToken() }
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
