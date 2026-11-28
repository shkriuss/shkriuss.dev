package dev.shkriuss.core.common

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Exact integer minor-unit monetary value (e.g., tetri/cents for 2-decimal currencies)
 * avoiding IEEE-754 floating-point drift in Budget and Trades apps.
 */
@JvmInline
value class MinorUnits(val amount: Long) : Comparable<MinorUnits> {
    operator fun plus(other: MinorUnits): MinorUnits = MinorUnits(Math.addExact(amount, other.amount))

    operator fun minus(other: MinorUnits): MinorUnits = MinorUnits(Math.subtractExact(amount, other.amount))

    operator fun unaryMinus(): MinorUnits = MinorUnits(Math.negateExact(amount))

    override fun compareTo(other: MinorUnits): Int = amount.compareTo(other.amount)

    fun toDecimalString(scale: Int = 2): String {
        require(scale in 0..8) { "scale must be between 0 and 8" }
        return BigDecimal.valueOf(amount, scale).toPlainString()
    }

    companion object {
        val ZERO = MinorUnits(0L)

        fun parseDecimal(input: String, scale: Int = 2): MinorUnits? {
            require(scale in 0..8) { "scale must be between 0 and 8" }
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return null
            val decimal = trimmed.toBigDecimalOrNull() ?: return null
            return runCatching {
                val scaled = decimal.setScale(scale, RoundingMode.UNNECESSARY)
                MinorUnits(scaled.movePointRight(scale).longValueExact())
            }.getOrNull()
        }
    }
}
