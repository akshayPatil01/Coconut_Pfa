package com.patil.coconut_pfa.domain

import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountInstitution
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.entity.FdDetailsEntity
import com.patil.coconut_pfa.data.local.entity.LoanDetailsEntity
import com.patil.coconut_pfa.data.local.entity.TxnEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.data.model.CardPaymentStatus
import com.patil.coconut_pfa.data.model.EmiScheduleItem
import kotlin.math.abs
import kotlin.math.min

object AccountDomainEngine {

    fun mapToDomainType(type: DbAccountType): AccountType = when (type) {
        DbAccountType.BANK -> AccountType.BANK
        DbAccountType.CREDIT_CARD -> AccountType.CREDIT_CARD
        DbAccountType.CASH -> AccountType.CASH
        DbAccountType.WALLET -> AccountType.WALLET
        DbAccountType.LOAN -> AccountType.LOAN
        DbAccountType.INVESTMENT -> AccountType.INVESTMENT
        DbAccountType.FD -> AccountType.FIXED_DEPOSIT
    }

    fun mapToDbType(type: AccountType): DbAccountType = when (type) {
        AccountType.BANK -> DbAccountType.BANK
        AccountType.CREDIT_CARD -> DbAccountType.CREDIT_CARD
        AccountType.CASH -> DbAccountType.CASH
        AccountType.WALLET -> DbAccountType.WALLET
        AccountType.LOAN -> DbAccountType.LOAN
        AccountType.INVESTMENT -> DbAccountType.INVESTMENT
        AccountType.FIXED_DEPOSIT -> DbAccountType.FD
    }

    fun mapInstitutionName(institution: DbAccountInstitution): String = when (institution) {
        DbAccountInstitution.HDFC -> "HDFC Bank"
        DbAccountInstitution.ICICI -> "ICICI Bank"
        DbAccountInstitution.SBI -> "SBI"
        DbAccountInstitution.AXIS -> "Axis Bank"
        DbAccountInstitution.OTHER -> "Other"
    }

    fun mapInstitutionToEnum(name: String): DbAccountInstitution = when (name.trim()) {
        "HDFC Bank", "HDFC" -> DbAccountInstitution.HDFC
        "ICICI Bank", "ICICI" -> DbAccountInstitution.ICICI
        "SBI", "State Bank of India" -> DbAccountInstitution.SBI
        "Axis Bank", "AXIS" -> DbAccountInstitution.AXIS
        else -> DbAccountInstitution.OTHER
    }

    fun buildAccountModel(
        entity: AccountEntity,
        cardDetails: CardDetailsEntity? = null,
        loanDetails: LoanDetailsEntity? = null,
        fdDetails: FdDetailsEntity? = null,
        valuations: List<ValuationEntity> = emptyList(),
        transactions: List<TxnEntity> = emptyList()
    ): Account {
        val accountType = mapToDomainType(entity.type)
        val bankName = mapInstitutionName(entity.institution)

        return when (accountType) {
            AccountType.BANK, AccountType.CASH, AccountType.WALLET -> {
                val balancePaise = BalanceCalculator.calculateAssetBalance(
                    openingBalancePaise = entity.openingBalancePaise,
                    accountId = entity.id,
                    transactions = transactions
                )
                Account(
                    id = entity.id,
                    name = entity.name,
                    type = accountType,
                    balanceInPaise = balancePaise,
                    accountNumberLast4 = entity.last4,
                    bankName = bankName,
                    includeInNetWorth = entity.includeInNetWorth,
                    archived = entity.archived
                )
            }

            AccountType.CREDIT_CARD -> {
                val openingOutstanding = abs(entity.openingBalancePaise)
                val limit = cardDetails?.creditLimitPaise ?: 0L
                val cardBal = BalanceCalculator.calculateCreditCardBalance(
                    openingOutstandingPaise = openingOutstanding,
                    creditLimitPaise = limit,
                    accountId = entity.id,
                    transactions = transactions
                )
                val unbilled = CardCycleCalculator.calculateUnbilledAmount(
                    accountId = entity.id,
                    lastStatementDate = null,
                    transactions = transactions
                )
                val dueDay = cardDetails?.dueDay ?: 5
                val statementDay = cardDetails?.statementDay ?: 15

                Account(
                    id = entity.id,
                    name = entity.name,
                    type = accountType,
                    balanceInPaise = -cardBal.outstandingPaise,
                    unbilledAmountInPaise = unbilled,
                    creditLimitInPaise = limit,
                    statementDay = statementDay,
                    dueDate = "${dueDay}th of month",
                    cardStatus = CardPaymentStatus.UNPAID,
                    accountNumberLast4 = entity.last4,
                    bankName = bankName,
                    includeInNetWorth = entity.includeInNetWorth,
                    archived = entity.archived
                )
            }

            AccountType.LOAN -> {
                val principal = loanDetails?.principalPaise ?: abs(entity.openingBalancePaise)
                val annualRateBps = loanDetails?.annualRateBps ?: 0
                val emiPaise = loanDetails?.emiPaise ?: 0L
                val tenureMonths = loanDetails?.tenureMonths ?: 0
                val emiDay = loanDetails?.emiDay ?: 10

                val paidTxnCount = transactions.count {
                    it.deletedAt == null && (it.toAccountId == entity.id || it.accountId == entity.id)
                }

                val emiSchedule = mutableListOf<EmiScheduleItem>()
                var remainingPrincipal = principal
                var totalPrincipalPaid = 0L

                for (i in 1..tenureMonths) {
                    val split = EmiCalculator.calculateEmiSplit(
                        outstandingPaise = remainingPrincipal,
                        annualRateBps = annualRateBps,
                        emiPaise = emiPaise
                    )
                    val prinPortion = min(split.principalPaise, remainingPrincipal)
                    val intPortion = split.interestPaise
                    val isPaid = i <= paidTxnCount

                    if (isPaid) {
                        totalPrincipalPaid += prinPortion
                    }
                    remainingPrincipal = (remainingPrincipal - prinPortion).coerceAtLeast(0L)

                    emiSchedule.add(
                        EmiScheduleItem(
                            installmentNo = i,
                            dueDate = "$emiDay of Month $i",
                            emiAmountInPaise = emiPaise,
                            principalInPaise = prinPortion,
                            interestInPaise = intPortion,
                            isPaid = isPaid
                        )
                    )
                }

                val outstanding = BalanceCalculator.calculateLoanOutstanding(principal, totalPrincipalPaid)

                Account(
                    id = entity.id,
                    name = entity.name,
                    type = accountType,
                    balanceInPaise = -outstanding,
                    emiAmountInPaise = emiPaise,
                    interestRate = annualRateBps / 100.0,
                    monthsLeft = (tenureMonths - paidTxnCount).coerceAtLeast(0),
                    emiSchedule = emiSchedule,
                    accountNumberLast4 = entity.last4,
                    bankName = bankName,
                    includeInNetWorth = entity.includeInNetWorth,
                    archived = entity.archived
                )
            }

            AccountType.FIXED_DEPOSIT -> {
                val principal = fdDetails?.principalPaise ?: entity.openingBalancePaise
                val liveValue = BalanceCalculator.calculateInvestmentOrFdValue(
                    openingBalancePaise = principal,
                    accountId = entity.id,
                    valuations = valuations,
                    transactions = transactions
                )
                val rateBps = fdDetails?.annualRateBps ?: 0

                Account(
                    id = entity.id,
                    name = entity.name,
                    type = accountType,
                    balanceInPaise = liveValue,
                    principalInPaise = principal,
                    fdInterestRate = rateBps / 100.0,
                    fdStartDate = fdDetails?.startDate ?: entity.openingDate,
                    maturityDate = fdDetails?.maturityDate,
                    maturityAmountInPaise = fdDetails?.maturityAmountPaise ?: principal,
                    accountNumberLast4 = entity.last4,
                    bankName = bankName,
                    includeInNetWorth = entity.includeInNetWorth,
                    archived = entity.archived
                )
            }

            AccountType.INVESTMENT -> {
                val liveValue = BalanceCalculator.calculateInvestmentOrFdValue(
                    openingBalancePaise = entity.openingBalancePaise,
                    accountId = entity.id,
                    valuations = valuations,
                    transactions = transactions
                )

                Account(
                    id = entity.id,
                    name = entity.name,
                    type = accountType,
                    balanceInPaise = liveValue,
                    currentValueInPaise = liveValue,
                    accountNumberLast4 = entity.last4,
                    bankName = bankName,
                    includeInNetWorth = entity.includeInNetWorth,
                    archived = entity.archived
                )
            }
        }
    }
}
