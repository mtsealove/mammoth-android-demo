package com.example.mammothnative.data.repository

import com.example.mammothnative.BuildConfig
import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.domain.model.StampCard
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StampRepository @Inject constructor(
    private val api: MammothApi,
) {
    /**
     * 홈에 노출하는 매머드익스프레스 스탬프 카드.
     * 웹과 같이 카드가 없으면 10칸 0개로 보여준다.
     */
    suspend fun getExpressStampCard(): StampCard {
        val cards = api.getStampCards().cards
        val express = cards.firstOrNull { it.shopGroupId == BuildConfig.EXPRESS_SHOP_GROUP_ID }
        return StampCard(
            shopGroupId = express?.shopGroupId,
            totalSlots = express?.totalSlots?.takeIf { it > 0 } ?: DEFAULT_TOTAL_SLOTS,
            stampCount = express?.stampCount ?: 0,
        )
    }

    private companion object {
        const val DEFAULT_TOTAL_SLOTS = 10
    }
}
