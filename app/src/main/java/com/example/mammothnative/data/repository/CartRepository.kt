package com.example.mammothnative.data.repository

import com.example.mammothnative.BuildConfig
import com.example.mammothnative.core.ApiException
import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.data.remote.dto.AddToCartRequest
import com.example.mammothnative.data.remote.dto.CartOptionRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val api: MammothApi,
) {
    /** 매장의 장바구니에 담긴 총 수량. 장바구니가 없으면 0. */
    suspend fun getCartCount(shopId: String): Int {
        // 담긴 게 없으면 서버가 404 를 준다. 오류가 아니라 "장바구니 없음"으로 본다.
        val cartId = try {
            api.getCartId(shopId).cartId
        } catch (e: ApiException) {
            if (e.statusCode == 404) null else throw e
        } ?: return 0
        return api.getCart(shopId, cartId).items.sumOf { it.qty }
    }

    /** [options] 는 (옵션 id, 수량) 쌍. 퍼스널 옵션은 수량이 2 이상일 수 있다. */
    suspend fun addToCart(
        shopId: String,
        itemId: String,
        quantity: Int,
        options: List<Pair<String, Int>>,
    ) {
        api.addToCart(
            shopId = shopId,
            body = AddToCartRequest(
                projectId = BuildConfig.PROJECT_ID,
                itemId = itemId,
                quantity = quantity,
                options = options.map { (optionId, optionQuantity) ->
                    CartOptionRequest(optionId = optionId, quantity = optionQuantity)
                },
            ),
        )
    }
}
