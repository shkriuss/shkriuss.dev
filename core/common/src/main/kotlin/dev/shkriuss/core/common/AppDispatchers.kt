package dev.shkriuss.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Injectable coroutine dispatchers for deterministic unit testing and clean threading.
 */
interface AppDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

object DefaultAppDispatchers : AppDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}

/**
 * Injectable wall-clock abstraction for deterministic time, backup, and HLC testing.
 */
fun interface Clock {
    fun nowEpochMillis(): Long
}

object SystemClock : Clock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
