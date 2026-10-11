package com.example.mammothnative.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * 서버 데이터 한 건의 상태.
 *
 * 웹의 react-query 는 `isLoading / data / error` 를 훅이 돌려줬다.
 * 안드로이드에서는 그 역할을 ViewModel 의 StateFlow 가 하고,
 * 이 타입이 "한 쿼리의 상태"를 표현한다.
 */
sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data class Success<T>(val data: T) : Loadable<T>
    data class Failure(val throwable: Throwable) : Loadable<Nothing>

    val dataOrNull: T?
        get() = (this as? Success)?.data
}

/** 성공 값만 다른 타입으로 바꾼다. */
fun <T, R> Loadable<T>.mapData(transform: (T) -> R): Loadable<R> = when (this) {
    is Loadable.Loading -> Loadable.Loading
    is Loadable.Failure -> this
    is Loadable.Success -> Loadable.Success(transform(data))
}

/** 값 Flow 를 로딩/실패까지 담은 Flow 로 감싼다. */
fun <T> Flow<T>.asLoadable(): Flow<Loadable<T>> = this
    .map<T, Loadable<T>> { Loadable.Success(it) }
    .onStart { emit(Loadable.Loading) }
    .catch { emit(Loadable.Failure(it)) }
