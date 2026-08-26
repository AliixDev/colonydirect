package com.colonydirect.common

/**
 * Money is always represented in integer minor units (e.g. paisa) to avoid
 * floating-point rounding errors in financial calculations and reconciliation.
 * Never construct Money from a Double in production code paths.
 */
@JvmInline
value class Money private constructor(val minorUnits: Long) : Comparable<Money> {

    init {
        require(minorUnits >= 0) { "Money cannot be negative: $minorUnits" }
    }

    operator fun plus(other: Money): Money = Money(minorUnits + other.minorUnits)

    operator fun minus(other: Money): Money {
        val result = minorUnits - other.minorUnits
        require(result >= 0) { "Money subtraction would be negative: $minorUnits - ${other.minorUnits}" }
        return Money(result)
    }

    operator fun times(quantity: Int): Money {
        require(quantity >= 0) { "Quantity cannot be negative: $quantity" }
        return Money(minorUnits * quantity)
    }

    /** Returns true if this amount is within [percentTolerance]% of [reference]. */
    fun isWithinVariance(reference: Money, percentTolerance: Int): Boolean {
        require(percentTolerance >= 0) { "Variance tolerance cannot be negative" }
        val allowedDelta = (reference.minorUnits * percentTolerance) / 100
        val actualDelta = kotlin.math.abs(minorUnits - reference.minorUnits)
        return actualDelta <= allowedDelta
    }

    override fun compareTo(other: Money): Int = minorUnits.compareTo(other.minorUnits)

    override fun toString(): String = minorUnits.toString()

    companion object {
        val ZERO = Money(0)

        fun ofMinorUnits(minorUnits: Long): Money = Money(minorUnits)

        /** Use only at system boundaries (e.g. parsing a rupee-denominated admin input). */
        fun ofRupees(rupees: Long): Money = Money(rupees * 100)
    }
}
