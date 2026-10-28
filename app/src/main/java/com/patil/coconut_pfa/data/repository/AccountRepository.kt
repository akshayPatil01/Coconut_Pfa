package com.patil.coconut_pfa.data.repository

import com.patil.coconut_pfa.data.local.dao.AccountDao
import com.patil.coconut_pfa.data.local.dao.CardDetailsDao
import com.patil.coconut_pfa.data.local.dao.CardStatementDao
import com.patil.coconut_pfa.data.local.dao.FdDetailsDao
import com.patil.coconut_pfa.data.local.dao.LoanDetailsDao
import com.patil.coconut_pfa.data.local.dao.ValuationDao
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.CardStatementEntity
import com.patil.coconut_pfa.data.local.entity.FdDetailsEntity
import com.patil.coconut_pfa.data.local.entity.LoanDetailsEntity
import com.patil.coconut_pfa.data.local.entity.ValuationEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountInstitution
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.flow.Flow

class AccountRepository(
    private val accountDao: AccountDao,
    private val cardDetailsDao: CardDetailsDao,
    private val cardStatementDao: CardStatementDao,
    private val loanDetailsDao: LoanDetailsDao,
    private val fdDetailsDao: FdDetailsDao,
    private val valuationDao: ValuationDao
) {
    fun getAllAccounts(): Flow<List<AccountEntity>> = accountDao.getAllAccountsFlow()
    fun getActiveAccounts(): Flow<List<AccountEntity>> = accountDao.getActiveAccountsFlow()
    suspend fun getAccountById(id: String): AccountEntity? = accountDao.getAccountById(id)
    fun getAccountByIdFlow(id: String): Flow<AccountEntity?> = accountDao.getAccountByIdFlow(id)

    suspend fun saveAccount(account: AccountEntity) = accountDao.insert(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)
    suspend fun deleteAccount(account: AccountEntity) = accountDao.delete(account)
    suspend fun deleteAccountById(id: String) = accountDao.deleteById(id)
    suspend fun setAccountArchived(id: String, archived: Boolean) = accountDao.setArchived(id, archived)

    suspend fun getCardDetails(accountId: String): CardDetailsEntity? = cardDetailsDao.getByAccountId(accountId)
    fun getCardDetailsFlow(accountId: String): Flow<CardDetailsEntity?> = cardDetailsDao.getByAccountIdFlow(accountId)
    suspend fun saveCardDetails(cardDetails: CardDetailsEntity) = cardDetailsDao.insert(cardDetails)

    fun getCardStatements(accountId: String): Flow<List<CardStatementEntity>> = cardStatementDao.getStatementsForAccountFlow(accountId)
    suspend fun saveCardStatement(statement: CardStatementEntity) = cardStatementDao.insert(statement)

    suspend fun getLoanDetails(accountId: String): LoanDetailsEntity? = loanDetailsDao.getByAccountId(accountId)
    fun getLoanDetailsFlow(accountId: String): Flow<LoanDetailsEntity?> = loanDetailsDao.getByAccountIdFlow(accountId)
    suspend fun saveLoanDetails(loanDetails: LoanDetailsEntity) = loanDetailsDao.insert(loanDetails)

    suspend fun getFdDetails(accountId: String): FdDetailsEntity? = fdDetailsDao.getByAccountId(accountId)
    fun getFdDetailsFlow(accountId: String): Flow<FdDetailsEntity?> = fdDetailsDao.getByAccountIdFlow(accountId)
    suspend fun saveFdDetails(fdDetails: FdDetailsEntity) = fdDetailsDao.insert(fdDetails)

    fun getValuations(accountId: String): Flow<List<ValuationEntity>> = valuationDao.getValuationsForAccountFlow(accountId)
    suspend fun getValuationsList(accountId: String): List<ValuationEntity> = valuationDao.getValuationsForAccount(accountId)
    suspend fun saveValuation(valuation: ValuationEntity) = valuationDao.insert(valuation)

    suspend fun createOnboardingAccounts(
        addBank: Boolean,
        addCard: Boolean,
        addCash: Boolean,
        addLoan: Boolean,
        addFd: Boolean
    ) {
        val currentDate = KolkataDateTimeUtils.currentDateString()

        if (addBank) {
            val bank = AccountEntity(
                id = "acc_1",
                name = "Demo Bank Salary",
                type = DbAccountType.BANK,
                institution = DbAccountInstitution.HDFC,
                last4 = "4321",
                openingBalancePaise = 24500000L,
                openingDate = currentDate,
                icon = "account_balance",
                colour = 0xFF1E88E5L
            )
            saveAccount(bank)
        }

        if (addCard) {
            val cardAcc = AccountEntity(
                id = "acc_3",
                name = "Card A Premier",
                type = DbAccountType.CREDIT_CARD,
                institution = DbAccountInstitution.HDFC,
                last4 = "1122",
                openingBalancePaise = -1850050L,
                openingDate = currentDate,
                icon = "credit_card",
                colour = 0xFFD81B60L
            )
            val cardDetails = CardDetailsEntity(
                accountId = "acc_3",
                creditLimitPaise = 15000000L,
                statementDay = 15,
                dueDay = 5
            )
            saveAccount(cardAcc)
            saveCardDetails(cardDetails)
        }

        if (addCash) {
            val cash = AccountEntity(
                id = "acc_5",
                name = "Cash Wallet",
                type = DbAccountType.CASH,
                institution = DbAccountInstitution.OTHER,
                openingBalancePaise = 450000L,
                openingDate = currentDate,
                icon = "payments",
                colour = 0xFF4CAF50L
            )
            saveAccount(cash)
        }

        if (addLoan) {
            val loanAcc = AccountEntity(
                id = "acc_loan_1",
                name = "Home Loan",
                type = DbAccountType.LOAN,
                institution = DbAccountInstitution.SBI,
                openingBalancePaise = -250000000L,
                openingDate = currentDate,
                icon = "account_balance",
                colour = 0xFFE65100L
            )
            val loanDetails = LoanDetailsEntity(
                accountId = "acc_loan_1",
                principalPaise = 250000000L,
                annualRateBps = 850,
                tenureMonths = 240,
                emiPaise = 2169600L,
                startDate = currentDate,
                emiDay = 10
            )
            saveAccount(loanAcc)
            saveLoanDetails(loanDetails)
        }

        if (addFd) {
            val fdAcc = AccountEntity(
                id = "acc_fd_1",
                name = "HDFC Fixed Deposit",
                type = DbAccountType.FD,
                institution = DbAccountInstitution.HDFC,
                openingBalancePaise = 10000000L,
                openingDate = currentDate,
                icon = "savings",
                colour = 0xFF7B1FA2L
            )
            val fdDetails = FdDetailsEntity(
                accountId = "acc_fd_1",
                principalPaise = 10000000L,
                annualRateBps = 720,
                startDate = currentDate,
                maturityDate = "2025-12-31",
                maturityAmountPaise = 10720000L
            )
            saveAccount(fdAcc)
            saveFdDetails(fdDetails)
        }
    }
}
