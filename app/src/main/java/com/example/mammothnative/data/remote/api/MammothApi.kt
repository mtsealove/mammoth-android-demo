package com.example.mammothnative.data.remote.api

import com.example.mammothnative.data.remote.dto.ActiveOrderListDto
import com.example.mammothnative.data.remote.dto.BannerListDto
import com.example.mammothnative.data.remote.dto.ConditionalCopyDto
import com.example.mammothnative.data.remote.dto.HomeSettingsDto
import com.example.mammothnative.data.remote.dto.MyMenuListDto
import com.example.mammothnative.data.remote.dto.RecentOrderListDto
import com.example.mammothnative.data.remote.dto.AddToCartRequest
import com.example.mammothnative.data.remote.dto.CartDto
import com.example.mammothnative.data.remote.dto.CartIdDto
import com.example.mammothnative.data.remote.dto.ItemCategoriesDto
import com.example.mammothnative.data.remote.dto.ItemDetailDto
import com.example.mammothnative.data.remote.dto.ItemsDto
import com.example.mammothnative.data.remote.dto.ShopDetailDto
import com.example.mammothnative.data.remote.dto.ShopDto
import com.example.mammothnative.data.remote.dto.SliceDto
import com.example.mammothnative.data.remote.dto.StampCardListDto
import com.example.mammothnative.data.remote.dto.UnreadDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 웹 `src/apis` 아래 Api 클래스들에 흩어져 있던 엔드포인트 중 메인 화면에서 쓰는 것들.
 * 화면이 늘어나면 도메인별 인터페이스(OrderApi, GiftApi ...)로 나눈다.
 */
interface MammothApi {

    /** 홈 설정(배경, 퀵메뉴 구성) */
    @GET("v0/home")
    suspend fun getHomeSettings(): HomeSettingsDto

    /** 상황별 문구 + 연결 메뉴 */
    @GET("v0/conditional-copy")
    suspend fun getConditionalCopy(): ConditionalCopyDto

    @GET("v0/banners")
    suspend fun getBanners(@Query("bannerType") bannerType: String): BannerListDto

    /**
     * 매장 목록. 주변 매장은 좌표와 locationAgreed 가 필요하고,
     * MY매장은 myShop=true 로 조회한다(로그인 필요).
     */
    @GET("v0/shops")
    suspend fun getShops(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("locationAgreed") locationAgreed: Boolean,
        @Query("searchType") searchType: String,
        @Query("query") query: String? = null,
        @Query("myShop") myShop: Boolean? = null,
    ): SliceDto<ShopDto>

    /** 매장 상세 */
    @GET("v0/shop/{shopId}")
    suspend fun getShop(@Path("shopId") shopId: String): ShopDetailDto

    /** 매장의 메뉴 카테고리 */
    @GET("v1/shop/{shopId}/categories")
    suspend fun getItemCategories(@Path("shopId") shopId: String): ItemCategoriesDto

    /** 카테고리별 메뉴 목록 */
    @GET("v1/shop/{shopId}/items")
    suspend fun getItems(
        @Path("shopId") shopId: String,
        @Query("categoryId") categoryId: String,
    ): ItemsDto

    /** 메뉴 상세(옵션 그룹 포함) */
    @GET("v1/item/{itemId}")
    suspend fun getItem(@Path("itemId") itemId: String): ItemDetailDto

    /** 매장의 장바구니 id — 로그인 필요 */
    @GET("v0/shop/{shopId}/cart")
    suspend fun getCartId(@Path("shopId") shopId: String): CartIdDto

    /** 장바구니 내용 — 로그인 필요 */
    @GET("v0/shop/{shopId}/cart/{cartId}")
    suspend fun getCart(
        @Path("shopId") shopId: String,
        @Path("cartId") cartId: String,
    ): CartDto

    /** 장바구니 담기 — 로그인 필요 */
    @POST("v0/shop/{shopId}/cart/item")
    suspend fun addToCart(
        @Path("shopId") shopId: String,
        @Body body: AddToCartRequest,
    )

    /** 즐겨찾기(MY매장) 토글 — 로그인 필요 */
    @PUT("v0/my-shop/{shopId}")
    suspend fun toggleMyShop(@Path("shopId") shopId: String)

    /** 로그인 필요 */
    @GET("v0/orders/active")
    suspend fun getActiveOrders(): ActiveOrderListDto

    /** 로그인 필요 — 홈 퀵메뉴의 "최근 주문" */
    @GET("v1/orders/recent")
    suspend fun getRecentOrders(
        @Query("size") size: Int,
        @Query("months") months: Int,
    ): RecentOrderListDto

    /** 로그인 필요 — 홈 퀵메뉴의 "MY메뉴" */
    @GET("v0/mymenus")
    suspend fun getMyMenus(): MyMenuListDto

    /** 로그인 필요 */
    @GET("v0/stamp-cards")
    suspend fun getStampCards(): StampCardListDto

    /** 로그인 필요 */
    @GET("v0/notifications/unread/exists")
    suspend fun getHasUnreadNotification(): UnreadDto
}
