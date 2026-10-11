package com.example.mammothnative.core

import java.io.IOException

/**
 * 서버가 내려준 오류 본문(`{ code, message }`)을 담은 예외.
 * 웹 `models/error` 의 ErrorResponse 와 같은 역할이다.
 *
 * OkHttp 인터셉터에서 던지므로 IOException 을 상속한다(그 밖의 예외를 던지면
 * 호출이 비정상 종료된다).
 */
class ApiException(
    val statusCode: Int,
    val code: String? = null,
    override val message: String? = null,
) : IOException(message) {
    val isUnauthorized: Boolean get() = statusCode == 401
}
