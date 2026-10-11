package com.example.mammothnative.data.repository

import com.example.mammothnative.data.remote.api.MammothApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val api: MammothApi,
) {
    suspend fun hasUnread(): Boolean = api.getHasUnreadNotification().hasUnread
}
