package com.example.mammothnative.data.repository

import com.example.mammothnative.data.remote.api.MammothApi
import com.example.mammothnative.domain.model.Banner
import com.example.mammothnative.domain.model.BannerLinkType
import com.example.mammothnative.domain.model.BannerType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerRepository @Inject constructor(
    private val api: MammothApi,
) {
    suspend fun getBanners(type: BannerType): List<Banner> =
        api.getBanners(type.name).banners.map {
            Banner(
                id = it.id,
                imageUrl = it.imageUrl,
                linkType = BannerLinkType.from(it.bannerLinkType),
                linkUrl = it.bannerLinkUrl,
                contentType = it.contentType,
                contentId = it.contentId,
            )
        }
}
