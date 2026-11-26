package dev.shkriuss.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommonCoreTest {
    @Test
    fun `appResult map and flatMap preserve errors`() {
        val ok: AppResult<Int> = AppResult.Success(21)
        assertEquals(AppResult.Success(42), ok.map { it * 2 })

        val failed: AppResult<Int> = AppResult.Failure(AppError.Offline("Network revoked"))
        val chained = failed.flatMap { AppResult.Success(it + 1) }
        assertEquals(failed, chained)
        assertNull(failed.getOrNull())
    }

    @Test
    fun `hybridLogicalClock stays strictly monotonic when physical clock freezes or rewinds`() {
        var now = 1_000L
        val hlc = HybridLogicalClock(nodeId = "device-a", clock = Clock { now })

        val t1 = hlc.tick()
        val t2 = hlc.tick()
        now = 900L // Wall clock moved backwards
        val t3 = hlc.tick()
        now = 2_000L
        val t4 = hlc.tick()

        assertTrue(t1 < t2)
        assertTrue(t2 < t3)
        assertTrue(t3 < t4)
        assertEquals(HlcTimestamp(1_000L, 0, "device-a"), t1)
        assertEquals(HlcTimestamp(1_000L, 1, "device-a"), t2)
        assertEquals(HlcTimestamp(1_000L, 2, "device-a"), t3)
        assertEquals(HlcTimestamp(2_000L, 0, "device-a"), t4)
        assertEquals(t4, HlcTimestamp.parse(t4.toCanonicalString()))
    }

    @Test
    fun `hybridLogicalClock rejects excessive future drift`() {
        val hlc = HybridLogicalClock(
            nodeId = "device-a",
            clock = Clock { 10_000L },
            maxDriftMillis = 5_000L,
        )
        val remote = HlcTimestamp(wallTimeMillis = 20_000L, logicalCounter = 0, nodeId = "device-b")
        val result = hlc.receive(remote)
        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun `minorUnits performs exact arithmetic without floating point drift`() {
        val a = MinorUnits.parseDecimal("19.99")!!
        val b = MinorUnits.parseDecimal("0.01")!!
        val sum = a + b
        assertEquals(MinorUnits(2000L), sum)
        assertEquals("20.00", sum.toDecimalString())
        assertEquals("-19.99", (-a).toDecimalString())
        assertNull(MinorUnits.parseDecimal("12.345", scale = 2))
    }
}
