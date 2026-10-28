package com.patil.coconut_pfa.domain

import kotlin.math.round

data class EmiSplit(
    val interestPaise: Long,
    val principalPaise: Long
)

object EmiCalculator {

    /**
     * Calculates interest and principal split for an EMI payment:
     * interest = round(outstanding * annualRateBps / 10000 / 12)
     * principal = EMI - interest
     */
    fun calculateEmiSplit(
        outstandingPaise: Long,
        annualRateBps: Int,
        emiPaise: Long
    ): EmiSplit {
        if (outstandingPaise <= 0) {
            return EmiSplit(interestPaise = 0L, principalPaise = emiPaise)
        }
        val interestDouble = (outstandingPaise.toDouble() * annualRateBps.toDouble()) / 10000.0 / 12.0
        val interestPaise = round(interestDouble).toLong()
        val principalPaise = emiPaise - interestPaise
        return EmiSplit(
            interestPaise = interestPaise,
            principalPaise = principalPaise
        )
    }
}
