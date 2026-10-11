package com.example.mammothnative.data.remote.dto

import kotlinx.serialization.Serializable

/** GET /v0/notifications/unread/exists */
@Serializable
data class UnreadDto(
    val hasUnread: Boolean = false,
)
