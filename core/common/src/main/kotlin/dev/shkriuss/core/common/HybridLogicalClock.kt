package dev.shkriuss.core.common

/**
 * Immutable Hybrid Logical Clock (HLC) timestamp for causal ordering across
 * local edits and future optional zero-knowledge multi-device sync.
 */
data class HlcTimestamp(
    val wallTimeMillis: Long,
    val logicalCounter: Int,
    val nodeId: String,
) : Comparable<HlcTimestamp> {
    init {
        require(wallTimeMillis >= 0L) { "wallTimeMillis must be non-negative" }
        require(logicalCounter >= 0) { "logicalCounter must be non-negative" }
        require(nodeId.isNotBlank()) { "nodeId must not be blank" }
    }

    override fun compareTo(other: HlcTimestamp): Int {
        val wallCmp = wallTimeMillis.compareTo(other.wallTimeMillis)
        if (wallCmp != 0) return wallCmp
        val counterCmp = logicalCounter.compareTo(other.logicalCounter)
        if (counterCmp != 0) return counterCmp
        return nodeId.compareTo(other.nodeId)
    }

    fun toCanonicalString(): String = "$wallTimeMillis:$logicalCounter:$nodeId"

    companion object {
        fun parse(raw: String): HlcTimestamp? {
            val parts = raw.split(':', limit = 3)
            if (parts.size != 3) return null
            val wall = parts[0].toLongOrNull() ?: return null
            val counter = parts[1].toIntOrNull() ?: return null
            val node = parts[2]
            if (wall < 0L || counter < 0 || node.isBlank()) return null
            return HlcTimestamp(wall, counter, node)
        }
    }
}

/**
 * Thread-safe Hybrid Logical Clock generator.
 */
class HybridLogicalClock(
    private val nodeId: String,
    private val clock: Clock = SystemClock,
    private val maxDriftMillis: Long = 60_000L,
) {
    init {
        require(nodeId.isNotBlank()) { "nodeId must not be blank" }
        require(maxDriftMillis >= 0L) { "maxDriftMillis must be non-negative" }
    }

    private var lastWallTime: Long = 0L
    private var lastCounter: Int = 0

    @Synchronized
    fun tick(): HlcTimestamp {
        val physical = clock.nowEpochMillis().coerceAtLeast(0L)
        if (physical > lastWallTime) {
            lastWallTime = physical
            lastCounter = 0
        } else {
            lastCounter += 1
        }
        return HlcTimestamp(lastWallTime, lastCounter, nodeId)
    }

    @Synchronized
    fun receive(remote: HlcTimestamp): AppResult<HlcTimestamp> {
        val physical = clock.nowEpochMillis().coerceAtLeast(0L)
        if (remote.wallTimeMillis - physical > maxDriftMillis) {
            return AppResult.Failure(
                AppError.Validation("Remote HLC timestamp exceeds allowed clock drift"),
            )
        }
        val nextWall = maxOf(physical, lastWallTime, remote.wallTimeMillis)
        val nextCounter = when {
            nextWall == lastWallTime && nextWall == remote.wallTimeMillis ->
                maxOf(lastCounter, remote.logicalCounter) + 1
            nextWall == lastWallTime -> lastCounter + 1
            nextWall == remote.wallTimeMillis -> remote.logicalCounter + 1
            else -> 0
        }
        lastWallTime = nextWall
        lastCounter = nextCounter
        return AppResult.Success(HlcTimestamp(lastWallTime, lastCounter, nodeId))
    }
}
