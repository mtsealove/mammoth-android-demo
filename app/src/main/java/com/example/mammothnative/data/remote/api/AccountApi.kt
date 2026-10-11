package com.example.mammothnative.data.remote.api

import com.example.mammothnative.data.remote.dto.RefreshTokenRequest
import com.example.mammothnative.data.remote.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** 로그인한 사용자용 엔드포인트. */
interface AccountApi {
    @GET("v0/user")
    suspend fun getUserProfile(): UserProfileDto

    @POST("v0/auth/signout")
    suspend fun signOut(@Body body: RefreshTokenRequest)
}
