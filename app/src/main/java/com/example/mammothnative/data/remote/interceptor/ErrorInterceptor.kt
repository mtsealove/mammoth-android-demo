package com.example.mammothnative.data.remote.interceptor

import com.example.mammothnative.core.ApiException
import com.example.mammothnative.data.remote.dto.ErrorDto
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 실패 응답을 서버 오류 본문이 담긴 [ApiException] 으로 바꾼다.
 * 화면에서 HTTP 코드 대신 서버 메시지를 그대로 보여줄 수 있다.
 */
class ErrorInterceptor @Inject constructor(
    private val json: Json,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response

        val body = response.peekBody(MAX_ERROR_BODY_BYTES).string()
        val error = runCatching { json.decodeFromString<ErrorDto>(body) }.getOrNull()
        throw ApiException(
            statusCode = response.code,
            code = error?.code,
            message = error?.message ?: response.message,
        )
    }

    private companion object {
        const val MAX_ERROR_BODY_BYTES = 1024L * 8
    }
}
