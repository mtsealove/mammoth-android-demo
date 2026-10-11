package com.example.mammothnative.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.mammothnative.domain.model.UserLocation
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 기기 좌표를 한 번 읽어온다.
 *
 * 권한이 없거나 측위에 실패하면 [UserLocation.Unavailable] 을 돌려준다.
 * 호출하는 쪽은 실패를 따로 처리하지 않고 그대로 매장 조회에 넘기면 된다(웹과 같은 동작).
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    fun hasPermission(): Boolean = LOCATION_PERMISSIONS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun current(): UserLocation {
        if (!hasPermission()) return UserLocation.Unavailable

        // 마지막 좌표가 있으면 바로 쓰고(즉시 응답), 없을 때만 새로 측위한다.
        return lastLocation() ?: requestCurrentLocation()
    }

    @Suppress("MissingPermission")
    private suspend fun lastLocation(): UserLocation? = suspendCancellableCoroutine { cont ->
        client.lastLocation
            .addOnSuccessListener { location ->
                cont.resume(location?.let { UserLocation(it.latitude, it.longitude, true) })
            }
            .addOnFailureListener { cont.resume(null) }
    }

    @Suppress("MissingPermission")
    private suspend fun requestCurrentLocation(): UserLocation =
        suspendCancellableCoroutine { cont ->
            val cancellation = CancellationTokenSource()
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMaxUpdateAgeMillis(MAX_LOCATION_AGE_MS)
                .build()
            client.getCurrentLocation(request, cancellation.token)
                .addOnSuccessListener { location ->
                    cont.resume(
                        location?.let { UserLocation(it.latitude, it.longitude, true) }
                            ?: UserLocation.Unavailable,
                    )
                }
                .addOnFailureListener { cont.resume(UserLocation.Unavailable) }
            cont.invokeOnCancellation { cancellation.cancel() }
        }

    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        private const val MAX_LOCATION_AGE_MS = 60_000L
    }
}
