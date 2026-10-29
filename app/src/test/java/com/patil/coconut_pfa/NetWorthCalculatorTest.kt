package com.patil.coconut_pfa

import com.patil.coconut_pfa.domain.AccountNetWorthItem
import com.patil.coconut_pfa.domain.NetWorthCalculator
import com.patil.coconut_pfa.domain.PersonLedgerItem
import org.junit.Assert.assertEquals
import org.junit.Test

class NetWorthCalculatorTest {

    @Test
    fun testNetWorthCalculationWithAssetsLiabilitiesAndLedger() {
        val accounts = listOf(
            AccountNetWorthItem(
                id = "acc_bank",
                name = "Bank Account",
                isLiabilityAccount = false,
                balancePaise = 24500000L, // Asset: ₹2,45,000
                includeInNetWorth = true
            ),
            AccountNetWorthItem(
                id = "acc_cash",
                name = "Cash",
                isLiabilityAccount = false,
                balancePaise = 1500000L, // Asset: ₹15,000
                includeInNetWorth = true
            ),
            AccountNetWorthItem(
                id = "acc_inv",
                name = "Mutual Funds",
                isLiabilityAccount = false,
                balancePaise = 52000000L, // Asset: ₹5,20,000
                includeInNetWorth = true
            ),
            AccountNetWorthItem(
                id = "acc_cc",
                name = "Credit Card",
                isLiabilityAccount = true,
                balancePaise = 1850050L, // Liability: ₹18,500.50 outstanding
                includeInNetWorth = true
            ),
            AccountNetWorthItem(
                id = "acc_loan",
                name = "Car Loan",
                isLiabilityAccount = true,
                balancePaise = 15000000L, // Liability: ₹1,50,000 outstanding
                includeInNetWorth = true
            ),
            AccountNetWorthItem(
                id = "acc_excluded",
                name = "Secret Savings",
                isLiabilityAccount = false,
                balancePaise = 99999999L, // Excluded from net worth!
                includeInNetWorth = false
            )
        )

        val personLedgers = listOf(
            PersonLedgerItem(
                personId = "p1",
                netBalancePaise = 350000L // Asset: Friend owes me ₹3,500
            ),
            PersonLedgerItem(
                personId = "p2",
                netBalancePaise = -120000L // Liability: I owe friend ₹1,200
            )
        )

        val result = NetWorthCalculator.calculateNetWorth(accounts, personLedgers)

        // Assets = Bank (24500000) + Cash (1500000) + Inv (52000000) + Ledger (350000) = 78350000 paise (₹7,83,500)
        assertEquals(78350000L, result.totalAssetsPaise)

        // Liabilities = CC (1850050) + Loan (15000000) + Ledger (120000) = 16970050 paise (₹1,69,700.50)
        assertEquals(16970050L, result.totalLiabilitiesPaise)

        // Net Worth = 78350000 - 16970050 = 61379950 paise (₹6,13,799.50)
        assertEquals(61379950L, result.netWorthPaise)
    }
}
