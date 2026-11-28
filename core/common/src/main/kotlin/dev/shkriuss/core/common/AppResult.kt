package dev.shkriuss.core.common

/**
 * Explicit outcome wrapper used across repositories and use cases so expected
 * failures (crypto errors, invalid input, missing permissions, offline state)
 * are handled structurally rather than crashing the UI.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.value
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.flatMap(transform: (T) -> AppResult<R>): AppResult<R> = when (this) {
    is AppResult.Success -> transform(value)
    is AppResult.Failure -> this
}

/**
 * Closed set of categorized domain errors safe to surface in UI without leaking
 * sensitive stack traces, raw keys, or user plaintext.
 */
sealed interface AppError {
    val message: String

    data class Validation(override val message: String) : AppError
    data class Crypto(override val message: String) : AppError
    data class Storage(override val message: String) : AppError
    data class PermissionDenied(override val message: String) : AppError
    data class NotFound(override val message: String) : AppError
    data class Offline(override val message: String) : AppError
    data class Unexpected(override val message: String) : AppError
}
