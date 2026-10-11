package com.example.mammothnative.ui.shop.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mammothnative.BuildConfig
import com.example.mammothnative.ui.component.MammothText
import com.example.mammothnative.ui.theme.MammothShapes
import com.example.mammothnative.ui.theme.MammothTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val MapHeight = 200.dp
private const val MAP_ZOOM = 15f

/**
 * 매장 위치 지도. 웹은 iNavi 지도를 쓰지만 네이티브에서는 구글 지도로 대체한다.
 * 핀 하나만 찍고 조작은 막아, 시트 안에서 스크롤과 충돌하지 않게 한다.
 */
@Composable
fun ShopMap(
    latitude: Double,
    longitude: Double,
    name: String,
    modifier: Modifier = Modifier,
) {
    val position = LatLng(latitude, longitude)

    if (BuildConfig.MAPS_API_KEY.isBlank()) {
        // 키가 없으면 지도가 빈 화면으로 뜨므로 안내로 대체한다.
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(MapHeight)
                .clip(MammothShapes.sm)
                .background(MammothTheme.colors.surface.background.bg2),
            contentAlignment = Alignment.Center,
        ) {
            MammothText(
                text = "지도를 보려면 local.properties 에\nMAPS_API_KEY 를 넣어 주세요",
                style = MammothTheme.typography.label,
                color = MammothTheme.colors.text.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
            )
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, MAP_ZOOM)
    }

    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .height(MapHeight)
            .clip(MammothShapes.sm),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = com.google.maps.android.compose.MapType.NORMAL),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
            mapToolbarEnabled = false,
        ),
    ) {
        Marker(state = MarkerState(position = position), title = name)
    }
}
