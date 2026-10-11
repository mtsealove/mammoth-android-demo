package com.example.mammothnative.data.remote.interceptor

import com.example.mammothnative.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** 모든 요청에 프로젝트 식별 헤더를 붙인다(웹 instance.ts 의 `Waldlust-Project-ID`). */
class ProjectHeaderInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Waldlust-Project-ID", BuildConfig.PROJECT_ID)
            .build()
        return chain.proceed(request)
    }
}
