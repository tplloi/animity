package com.kl3jvi.animity.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

// Network Result
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable? = null) : Result<Nothing>
    object Loading : Result<Nothing>
}

// Ui Result
sealed interface UiResult<out T> {
    data class Success<T>(val data: T) : UiResult<T>
    data class Error(val throwable: Throwable) : UiResult<Nothing>
    object Loading : UiResult<Nothing>
}

/* Converting a Flow<T> to a Flow<Result<T>>. */
/**
 * Part of Now In Android google Sample
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return map<T, Result<T>> {
        Result.Success(it)
    }.onStart {
        emit(Result.Loading)
    }.catch {
        emit(Result.Error(it))
    }
}

fun <T> Flow<T>.mapToUiState(
    coroutineScope: CoroutineScope
): StateFlow<UiResult<T>> {
    return map<T, UiResult<T>> {
        UiResult.Success(it)
    }.onStart {
        emit(UiResult.Loading)
    }.catch {
        emit(UiResult.Error(it))
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiResult.Loading
    )
}
