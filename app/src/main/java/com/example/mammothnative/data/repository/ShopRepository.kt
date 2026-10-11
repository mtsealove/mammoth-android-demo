package com.example.mammothnative.data.repository

import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.domain.model.Shop
import com.example.mammothnative.domain.model.ShopOrderStatus
import com.example.mammothnative.domain.model.ShopDetail
import com.example.mammothnative.domain.model.ShopPage
import com.example.mammothnative.domain.model.UserLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository @Inject constructor(
    private val api: MammothApi,
) {
    /**
     * 주변 매장. 좌표와 `locationAgreed` 를 함께 보낸다
     * (웹 useNearShops 와 같은 파라미터 구성).
     */
    suspend fun getNearShops(
        location: UserLocation,
        page: Int,
        query: String? = null,
    ): ShopPage = api.getShops(
        page = page,
        size = PAGE_SIZE,
        lat = location.latitude,
        lon = location.longitude,
        locationAgreed = location.agreed,
        searchType = SEARCH_TYPE_ORDER,
        query = query?.takeIf { it.isNotBlank() },
    ).toDomain()

    /** MY매장. 웹과 동일하게 좌표 없이 조회한다. */
    suspend fun getMyShops(page: Int): ShopPage = api.getShops(
        page = page,
        size = PAGE_SIZE,
        lat = 0.0,
        lon = 0.0,
        locationAgreed = false,
        searchType = SEARCH_TYPE_ORDER,
        myShop = true,
    ).toDomain()

    /** 매장 상세 */
    suspend fun getShopDetail(shopId: String): ShopDetail = api.getShop(shopId).let { dto ->
        ShopDetail(
            id = dto.id,
            name = dto.name.orEmpty(),
            address = dto.address.orEmpty(),
            phoneNumber = dto.phoneNumber?.takeIf(String::isNotBlank),
            openHours = dto.openhours?.takeIf(String::isNotBlank),
            orderStatus = ShopOrderStatus.from(dto.orderStatus),
            isMyShop = dto.isMyShop,
            latitude = dto.lat,
            longitude = dto.lon,
            kindName = dto.shopKindInfo?.name?.takeIf(String::isNotBlank),
            kindNote = dto.shopKindInfo?.note?.takeIf(String::isNotBlank),
            // 매장 종류 정보가 없으면 웹과 같이 둘 다 가능한 것으로 본다.
            useHall = dto.shopKindInfo?.diningOption?.useHall ?: true,
            useTakeout = dto.shopKindInfo?.diningOption?.useTakeout ?: true,
        )
    }

    /** 즐겨찾기 토글 (PUT /v0/my-shop/{shopId}) */
    suspend fun toggleMyShop(shopId: String) = api.toggleMyShop(shopId)

    private fun com.example.mammothnative.data.remote.dto.SliceDto<
        com.example.mammothnative.data.remote.dto.ShopDto,
        >.toDomain() = ShopPage(
        shops = content.map {
            Shop(
                id = it.id,
                name = it.name.orEmpty(),
                address = it.address.orEmpty(),
                thumbnailUrl = it.thumbnailImageUrl?.takeIf(String::isNotBlank),
                distanceText = it.distanceFromUser?.takeIf(String::isNotBlank),
                badges = it.badges,
                orderStatus = ShopOrderStatus.from(it.orderStatus),
                isMyShop = it.isMyShop,
                isTestShop = it.isTestShop,
            )
        },
        isLast = slice.last,
        currentPage = slice.currentPage,
    )

    private companion object {
        const val PAGE_SIZE = 10
        const val SEARCH_TYPE_ORDER = "ORDER"
    }
}
