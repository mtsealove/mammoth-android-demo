package com.example.mammothnative.di

import com.example.mammothnative.BuildConfig
import com.example.mammothnative.data.remote.api.AccountApi
import com.example.mammothnative.data.remote.api.AuthApi
import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.data.remote.interceptor.AuthInterceptor
import com.example.mammothnative.data.remote.interceptor.ErrorInterceptor
import com.example.mammothnative.data.remote.interceptor.ProjectHeaderInterceptor
import com.example.mammothnative.data.remote.interceptor.TokenAuthenticator
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** 토큰 재발급 전용(인증 인터셉터가 붙지 않은) 클라이언트 구분용. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthFree

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // 서버에 필드가 추가돼도 앱이 깨지지 않게 한다.
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        // 기본값과 같은 필드도 요청 본문에 담는다. 서버가 필수로 보는 값(옵션 수량 등)이
        // 기본값이라는 이유로 빠지면 400 이 난다.
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    @AuthFree
    fun provideAuthFreeClient(
        projectHeaderInterceptor: ProjectHeaderInterceptor,
        errorInterceptor: ErrorInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(errorInterceptor)
        .addInterceptor(projectHeaderInterceptor)
        .addInterceptor(loggingInterceptor)
        .applyTimeouts()
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        projectHeaderInterceptor: ProjectHeaderInterceptor,
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        // errorInterceptor 가 가장 바깥이라 재발급 재시도까지 끝난 최종 응답을 본다.
        .addInterceptor(errorInterceptor)
        .addInterceptor(projectHeaderInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .applyTimeouts()
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideMammothApi(retrofit: Retrofit): MammothApi = retrofit.create(MammothApi::class.java)

    @Provides
    @Singleton
    fun provideAccountApi(retrofit: Retrofit): AccountApi = retrofit.create(AccountApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(@AuthFree client: OkHttpClient, json: Json): AuthApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(AuthApi::class.java)

    private fun OkHttpClient.Builder.applyTimeouts() = this
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

    private const val TIMEOUT_SECONDS = 20L
}
