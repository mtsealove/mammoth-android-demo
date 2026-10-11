package com.example.mammothnative.data.repository

import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.domain.model.MyMenu
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMenuRepository @Inject constructor(
    private val api: MammothApi,
) {
    suspend fun getMyMenus(): List<MyMenu> = api.getMyMenus().myMenus.map {
        MyMenu(
            myMenuId = it.myMenuId,
            name = it.myMenuName.orEmpty(),
            shopName = it.shopName.orEmpty(),
            itemName = it.item?.itemName.orEmpty(),
            price = Math.round(it.item?.salePrice ?: 0.0).toInt(),
            thumbnailUrl = it.item?.thumbnailUrl,
        )
    }
}
