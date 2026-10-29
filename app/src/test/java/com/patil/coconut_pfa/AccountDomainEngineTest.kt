package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountInstitution
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.DbTxnSource
import com.patil.coconut_pfa.data.local.entity.DbTxnType
import com.patil.coconut_pfa.data.local.entity.LoanDetailsEntity
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.domain.AccountDomainEngine
import com.patil.coconut_pfa.domain.BalanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountDomainEngineTest {

    @Test
    fun mapToDomainType_mapsAllSevenTypesCorrectly() {
        assertEquals(AccountType.BANK, AccountDomainEngine.mapToDomainType(DbAccountType.BANK))
        assertEquals(AccountType.CREDIT_CARD, AccountDomainEngine.mapToDomainType(DbAccountType.CREDIT_CARD))
        assertEquals(AccountType.CASH, AccountDomainEngine.mapToDomainType(DbAccountType.CASH))
        assertEquals(AccountType.WALLET, AccountDomainEngine.mapToDomainType(DbAccountType.WALLET))
        assertEquals(AccountType.LOAN, AccountDomainEngine.mapToDomainType(DbAccountType.LOAN))
        assertEquals(AccountType.INVESTMENT, AccountDomainEngine.mapToDomainType(DbAccountType.INVESTMENT))
        assertEquals(AccountType.FIXED_DEPOSIT, AccountDomainEngine.mapToDomainType(DbAccountType.FD))
    }

    @Test
    fun buildAccountModel_forBankWithTransactionsAndReconciliation_calculatesCorrectBalance() {
        val bankEntity = AccountEntity(
            id = "acc_bank_1",
            name = "HDFC Salary Account",
            type = DbAccountType.BANK,
            institution = DbAccountInstitution.HDFC,
            last4 = "4321",
            openingBalancePaise = 1000000L, // ₹10,000.00
            openingDate = "2024-01-01",
            icon = "account_balance",
            colour = 0xFF1E88E5L
        )

        val transactions = listOf(
            TxnEntity(
                id = "tx_1",
                type = DbTxnType.INCOME,
                amountPaise = 500000L, // +₹5,000
                accountId = "acc_bank_1",
                occurredAt = 1000L,
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "tx_2",
                type = DbTxnType.EXPENSE,
                amountPaise = 200000L, // -₹2,000
                accountId = "acc_bank_1",
                occurredAt = 2000L,
                source = DbTxnSource.MANUAL
            ),
            TxnEntity(
                id = "tx_3",
                type = DbTxnType.ADJUSTMENT,
                amountPaise = -100000L, // -₹1,000 adjustment
                accountId = "acc_bank_1",
                occurredAt = 3000L,
                source = DbTxnSource.MANUAL
            )
        )

        val domainAccount = AccountDomainEngine.buildAccountModel(
            entity = bankEntity,
            transactions = transactions
        )

        assertEquals("HDFC Salary Account", domainAccount.name)
        assertEquals(AccountType.BANK, domainAccount.type)
        assertEquals("HDFC Bank", domainAccount.bankName)
        assertEquals("4321", domainAccount.accountNumberLast4)
        // 10,000 + 5,000 - 2,000 - 1,000 = 12,000 (1200000 paise)
        assertEquals(1200000L, domainAccount.balanceInPaise)
    }

    @Test
    fun buildAccountModel_forCreditCard_calculatesOutstandingAndAvailableCredit() {
        val cardEntity = AccountEntity(
            id = "acc_cc_1",
            name = "Regalia Credit Card",
            type = DbAccountType.CREDIT_CARD,
            institution = DbAccountInstitution.HDFC,
            last4 = "9988",
            openingBalancePaise = -500000L, // ₹5,000 outstanding
            openingDate = "2024-01-01",
            icon = "credit_card",
            colour = 0xFFD81B60L
        )

        val cardDetails = CardDetailsEntity(
            accountId = "acc_cc_1",
            creditLimitPaise = 10000000L, // ₹100,000 limit
            statementDay = 15,
            dueDay = 5
        )

        val transactions = listOf(
            TxnEntity(
                id = "tx_cc_1",
                type = DbTxnType.EXPENSE,
                amountPaise = 1500000L, // +₹15,000 expense
                accountId = "acc_cc_1",
                occurredAt = 1000L,
                source = DbTxnSource.MANUAL
            )
        )

        val domainAccount = AccountDomainEngine.buildAccountModel(
            entity = cardEntity,
            cardDetails = cardDetails,
            transactions = transactions
        )

        assertEquals(AccountType.CREDIT_CARD, domainAccount.type)
        // Total outstanding = 5,000 + 15,000 = 20,000 (represented as -2000000 paise)
        assertEquals(-2000000L, domainAccount.balanceInPaise)
        assertEquals(10000000L, domainAccount.creditLimitInPaise)
        // Available credit = 100,000 - 20,000 = 80,000 (8000000 paise)
        assertEquals(8000000L, domainAccount.availableCreditInPaise)
    }

    @Test
    fun buildAccountModel_forLoan_calculatesEmiScheduleAndOutstanding() {
        val loanEntity = AccountEntity(
            id = "acc_loan_1",
            name = "Car Loan",
            type = DbAccountType.LOAN,
            institution = DbAccountInstitution.SBI,
            openingBalancePaise = -100000000L, // ₹1,000,000 principal
            openingDate = "2024-01-01",
            icon = "payments",
            colour = 0xFFE65100L
        )

        val loanDetails = LoanDetailsEntity(
            accountId = "acc_loan_1",
            principalPaise = 100000000L,
            annualRateBps = 1000, // 10.0% p.a.
            tenureMonths = 12,
            emiPaise = 8791500L, // ₹87,915
            startDate = "2024-01-01",
            emiDay = 10
        )

        val transactions = listOf(
            TxnEntity(
                id = "tx_loan_1",
                type = DbTxnType.TRANSFER,
                amountPaise = 8791500L,
                toAccountId = "acc_loan_1",
                occurredAt = 1000L,
                source = DbTxnSource.MANUAL
            )
        )

        val domainAccount = AccountDomainEngine.buildAccountModel(
            entity = loanEntity,
            loanDetails = loanDetails,
            transactions = transactions
        )

        assertEquals(AccountType.LOAN, domainAccount.type)
        assertEquals(12, domainAccount.emiSchedule.size)
        assertTrue(domainAccount.emiSchedule[0].isPaid)
        assertFalse(domainAccount.emiSchedule[1].isPaid)
        // Loan balance is negative outstanding
        assertTrue(domainAccount.balanceInPaise < 0)
    }

    @Test
    fun buildAccountModel_forInvestment_updatesValuationCorrectly() {
        val invEntity = AccountEntity(
            id = "acc_inv_1",
            name = "Mutual Funds",
            type = DbAccountType.INVESTMENT,
            institution = DbAccountInstitution.OTHER,
            openingBalancePaise = 5000000L, // ₹50,000
            openingDate = "2024-01-01",
            icon = "show_chart",
            colour = 0xFF2E7D32L
        )

        val valuations = listOf(
            ValuationEntity(
                id = "val_1",
                accountId = "acc_inv_1",
                date = "2024-02-01",
                valuePaise = 5500000L // ₹55,000 updated market value
            )
        )

        val domainAccount = AccountDomainEngine.buildAccountModel(
            entity = invEntity,
            valuations = valuations
        )

        assertEquals(5500000L, domainAccount.balanceInPaise)
        assertEquals(5500000L, domainAccount.currentValueInPaise)
    }

    @Test
    fun buildAccountModel_forInvestment_withMultipleValuations_picksLatestValuation() {
        val invEntity = AccountEntity(
            id = "acc_inv_2",
            name = "Stocks Portfolio",
            type = DbAccountType.INVESTMENT,
            institution = DbAccountInstitution.OTHER,
            openingBalancePaise = 10000000L, // ₹100,000
            openingDate = "2024-01-01",
            icon = "trending_up",
            colour = 0xFF2E7D32L
        )

        val valuations = listOf(
            ValuationEntity(
                id = "val_old",
                accountId = "acc_inv_2",
                date = "2024-02-01",
                valuePaise = 11000000L // ₹110,000
            ),
            ValuationEntity(
                id = "val_new",
                accountId = "acc_inv_2",
                date = "2024-03-01",
                valuePaise = 12500000L // ₹125,000
            )
        )

        val domainAccount = AccountDomainEngine.buildAccountModel(
            entity = invEntity,
            valuations = valuations
        )

        assertEquals(12500000L, domainAccount.balanceInPaise)
        assertEquals(12500000L, domainAccount.currentValueInPaise)
    }
}
