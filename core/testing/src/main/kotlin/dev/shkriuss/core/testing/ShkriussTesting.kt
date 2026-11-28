package dev.shkriuss.core.testing

import dev.shkriuss.core.common.AppDispatchers
import dev.shkriuss.core.common.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * Deterministic dispatcher bundle for coroutine unit tests.
 */
class TestAppDispatchers(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : AppDispatchers {
    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
}

/**
 * Mutable deterministic clock for unit tests.
 */
class FakeClock(private var currentEpochMillis: Long = 1_764_000_000_000L) : Clock {
    override fun nowEpochMillis(): Long = currentEpochMillis

    fun advanceBy(millis: Long) {
        currentEpochMillis += millis
    }

    fun set(epochMillis: Long) {
        currentEpochMillis = epochMillis
    }
}

/**
 * Standard Robolectric/Roborazzi device qualifier presets matching GrapheneOS
 * phone and tablet form factors plus 200% accessibility font scaling.
 */
object ShkriussDeviceSpecs {
    const val ROBOLECTRIC_SDK: Int = 36

    const val PHONE_LIGHT: String = "w411dp-h915dp-port-notnight-420dpi"
    const val PHONE_DARK: String = "w411dp-h915dp-port-night-420dpi"
    const val TABLET_LIGHT: String = "w1280dp-h800dp-land-notnight-320dpi"
    const val TABLET_DARK: String = "w1280dp-h800dp-land-night-320dpi"
    const val FONT_SCALE_ACCESSIBILITY: Float = 2.0f
}
