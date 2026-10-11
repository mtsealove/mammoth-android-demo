package com.example.mammothnative.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore by preferencesDataStore(name = "auth")

/**
 * 웹에서 쿠키(accessToken / refreshToken)로 들고 있던 토큰을 DataStore 에 보관한다.
 */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val accessTokenKey = stringPreferencesKey("accessToken")
    private val refreshTokenKey = stringPreferencesKey("refreshToken")

    val accessToken: Flow<String?> = context.tokenDataStore.data.map { it[accessTokenKey] }
    val refreshToken: Flow<String?> = context.tokenDataStore.data.map { it[refreshTokenKey] }

    /** 로그인 여부. 화면에서는 이 값으로 인증이 필요한 영역을 가린다. */
    val isSignedIn: Flow<Boolean> = refreshToken.map { !it.isNullOrBlank() }

    suspend fun currentAccessToken(): String? = accessToken.first()

    suspend fun currentRefreshToken(): String? = refreshToken.first()

    suspend fun save(accessToken: String, refreshToken: String) {
        context.tokenDataStore.edit {
            it[accessTokenKey] = accessToken
            it[refreshTokenKey] = refreshToken
        }
    }

    suspend fun clear() {
        context.tokenDataStore.edit { it.clear() }
    }
}
