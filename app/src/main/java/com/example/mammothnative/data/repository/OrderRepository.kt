package com.example.mammothnative.data.repository

import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.domain.model.ActiveOrder
import com.example.mammothnative.domain.model.OrderStatus
import com.example.mammothnative.domain.model.RecentOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val api: MammothApi,
) {
    /** 진행 중인 주문. 로그인 상태에서만 호출한다. */
    suspend fun getActiveOrders(): List<ActiveOrder> =
        api.getActiveOrders().orders.map {
            ActiveOrder(
                orderNo = it.orderNo,
                shopName = it.shopName.orEmpty(),
                displayOrderNo = it.displayOrderNo.orEmpty(),
                status = OrderStatus.from(it.status),
                thumbnailUrl = it.thumbNailImageUrl,
            )
        }

    /** 웹 useRecentOrders 와 같은 조건(최근 1개월, 5건). */
    suspend fun getRecentOrders(): List<RecentOrder> =
        api.getRecentOrders(size = RECENT_SIZE, months = RECENT_MONTHS).orders.map {
            RecentOrder(
                orderId = it.orderId,
                orderName = it.orderName.orEmpty(),
                shopName = it.shopName.orEmpty(),
                paymentAmount = Math.round(it.paymentAmount).toInt(),
            )
        }

    private companion object {
        const val RECENT_SIZE = 5
        const val RECENT_MONTHS = 1
    }
}
