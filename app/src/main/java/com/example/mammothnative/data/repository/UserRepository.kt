package com.example.mammothnative.data.repository

import com.example.mammothnative.data.remote.api.AccountApi
import com.example.mammothnative.domain.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: AccountApi,
) {
    suspend fun getProfile(): UserProfile = api.getUserProfile().let {
        UserProfile(
            id = it.id,
            nickname = it.nickname.orEmpty().ifBlank { "고객" },
            phoneNumber = it.phoneNumber,
            barcodeNo = it.barcodeNo,
            couponCount = it.couponCount,
            points = it.points ?: 0,
            stampCount = it.stampCount ?: 0,
        )
    }
}
