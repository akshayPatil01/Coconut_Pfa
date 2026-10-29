package com.patil.coconut_pfa.data.model

enum class BillingFrequency(val displayName: String, val monthlyMultiplier: Double) {
    MONTHLY("Monthly", 1.0),
    YEARLY("Yearly", 1.0 / 12.0),
    QUARTERLY("Quarterly", 1.0 / 3.0),
    WEEKLY("Weekly", 4.33)
}

data class Subscription(
    val id: String,
    val name: String,
    val priceInPaise: Long,
    val billingFrequency: BillingFrequency,
    val nextRenewalDate: String,
    val category: String = "Entertainment",
    val paymentAccountName: String = "Demo Bank Salary",
    val isActive: Boolean = true
) {
    val monthlyEquivalentInPaise: Long
        get() = (priceInPaise * billingFrequency.monthlyMultiplier).toLong()
}
