package com.example.mammothnative.domain.model

/**
 * 매장 조회에 함께 보내는 좌표.
 *
 * 웹은 네이티브 브리지로 좌표를 받아 redux 에 담고, 실패하면 (0,0) + `locationAgreed=false`
 * 로 조회했다. 여기서도 같은 규칙을 쓴다.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    /** 좌표를 실제로 확보했는지. 서버의 `locationAgreed` 파라미터로 나간다. */
    val agreed: Boolean,
) {
    companion object {
        /** 권한이 없거나 좌표를 못 받았을 때 (웹 `setDefaultLocation`) */
        val Unavailable = UserLocation(latitude = 0.0, longitude = 0.0, agreed = false)
    }
}
