package com.patil.coconut_pfa.domain

import kotlin.math.abs

data class AccountNetWorthItem(
    val id: String,
    val name: String,
    val isLiabilityAccount: Boolean, // true for Credit Card, Loan; false for Bank, Cash, Wallet, Investment, FD
    val balancePaise: Long, // positive asset balance, or positive outstanding for liabilities
    val includeInNetWorth: Boolean = true
)

data class PersonLedgerItem(
    val personId: String,
    val netBalancePaise: Long // > 0 owes me (asset), < 0 I owe (liability)
)

data class NetWorthSummary(
    val totalAssetsPaise: Long,
    val totalLiabilitiesPaise: Long,
    val netWorthPaise: Long
)

object NetWorthCalculator {

    /**
     * Calculates Net Worth:
     * Net worth = assets (bank, cash, wallet, investment, FD, positive ledger balances)
     *            - liabilities (card outstanding, loan outstanding, negative ledger balances),
     * only considering accounts where includeInNetWorth == true.
     */
    fun calculateNetWorth(
        accounts: List<AccountNetWorthItem>,
        personLedgers: List<PersonLedgerItem>
    ): NetWorthSummary {
        var totalAssets = 0L
        var totalLiabilities = 0L

        for (account in accounts) {
            if (!account.includeInNetWorth) continue

            if (account.isLiabilityAccount) {
                val outstanding = abs(account.balancePaise)
                if (outstanding > 0) {
                    totalLiabilities += outstanding
                }
            } else {
                if (account.balancePaise > 0) {
                    totalAssets += account.balancePaise
                } else if (account.balancePaise < 0) {
                    totalLiabilities += abs(account.balancePaise)
                }
            }
        }

        for (person in personLedgers) {
            if (person.netBalancePaise > 0) {
                totalAssets += person.netBalancePaise
            } else if (person.netBalancePaise < 0) {
                totalLiabilities += abs(person.netBalancePaise)
            }
        }

        val netWorth = totalAssets - totalLiabilities
        return NetWorthSummary(
            totalAssetsPaise = totalAssets,
            totalLiabilitiesPaise = totalLiabilities,
            netWorthPaise = netWorth
        )
    }
}
