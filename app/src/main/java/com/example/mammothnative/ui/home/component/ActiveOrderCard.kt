package com.example.mammothnative.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mammothnative.domain.model.ActiveOrder
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.example.mammothnative.ui.util.clickableNoRipple

/**
 * 웹 HomeFragment/components/ActiveOrders 의 카드 한 장.
 * 진행 중인 주문을 홈 하단에 띄운다.
 */
@Composable
fun ActiveOrderCard(
    order: ActiveOrder,
    isSingle: Boolean,
    onClick: (ActiveOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .then(if (isSingle) Modifier else Modifier.width(300.dp))
            .clip(MammothShapes.md)
            .background(MammothTheme.colors.surface.background.bg1)
            .clickableNoRipple { onClick(order) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (order.thumbnailUrl != null) {
            AsyncImage(
                model = order.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MammothShapes.sm),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            MammothText(
                text = order.statusMessage,
                style = MammothTheme.typography.body,
                fontWeight = FontWeight.Bold,
            )
            MammothText(
                text = order.shopName,
                style = MammothTheme.typography.label,
                color = MammothTheme.colors.text.secondary,
            )
        }
    }
}
