package com.example.mammothnative.data.repository

import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.data.remote.dto.QuickMenuItemDto
import com.example.mammothnative.domain.model.ConditionalCopy
import com.example.mammothnative.domain.model.HomeBackgroundSize
import com.example.mammothnative.domain.model.HomeBackgroundType
import com.example.mammothnative.domain.model.HomeSettings
import com.example.mammothnative.domain.model.QuickMenuItem
import com.example.mammothnative.domain.model.QuickMenuType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 웹 `homeQueries` / `conditionalCopyQueries` 에 해당한다.
 * DTO → 도메인 모델 변환은 여기서만 한다(화면은 서버 응답 모양을 몰라도 된다).
 */
@Singleton
class HomeRepository @Inject constructor(
    private val api: MammothApi,
) {
    suspend fun getHomeSettings(): HomeSettings {
        val dto = api.getHomeSettings()
        return HomeSettings(
            backgroundSize = HomeBackgroundSize.from(dto.bgSize),
            backgroundType = HomeBackgroundType.from(dto.bgType),
            backgroundColor = dto.bgColor,
            backgroundImageUrl = dto.bgImageUrl,
            quickMenuExpanded = dto.quickMenuExpanded,
            quickMenuItems = dto.quickMenuItems
                .map(QuickMenuItemDto::toDomain)
                .filter { it.type != QuickMenuType.UNKNOWN },
        )
    }

    suspend fun getConditionalCopy(): ConditionalCopy {
        val dto = api.getConditionalCopy()
        return ConditionalCopy(
            message = dto.message,
            menuType = dto.menuType?.let(QuickMenuType::from),
        )
    }
}

private fun QuickMenuItemDto.toDomain(): QuickMenuItem {
    val type = QuickMenuType.from(menuType)
    return QuickMenuItem(
        type = type,
        name = name?.takeIf { it.isNotBlank() } ?: type.label,
        iconUrl = iconUrl,
        targetId = targetId,
    )
}
