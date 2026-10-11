package com.example.mammothnative.ui.home.component

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.mammothnative.domain.model.Banner
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.util.clickableNoRipple

/** 웹 HomeFragment/components/Banners 의 배너 한 장 (4:1) */
@Composable
fun BannerItem(
    banner: Banner,
    onClick: (Banner) -> Unit,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = banner.imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f)
            .clip(MammothShapes.md)
            .clickableNoRipple { onClick(banner) },
    )
}
