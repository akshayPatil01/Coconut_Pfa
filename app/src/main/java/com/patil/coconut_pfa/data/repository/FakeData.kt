package com.patil.coconut_pfa.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.patil.coconut_pfa.data.model.Account
import com.patil.coconut_pfa.data.model.AccountType
import com.patil.coconut_pfa.data.model.BillKind
import com.patil.coconut_pfa.data.model.BillingFrequency
import com.patil.coconut_pfa.data.model.CardPaymentStatus
import com.patil.coconut_pfa.data.model.CategorySpendItem
import com.patil.coconut_pfa.data.model.CycleSpendItem
import com.patil.coconut_pfa.data.model.CycleSummary
import com.patil.coconut_pfa.data.model.DailySpendHeatmapItem
import com.patil.coconut_pfa.data.model.DueItem
import com.patil.coconut_pfa.data.model.DueTimeframe
import com.patil.coconut_pfa.data.model.EmiScheduleItem
import com.patil.coconut_pfa.data.model.Goal
import com.patil.coconut_pfa.data.model.GoalContribution
import com.patil.coconut_pfa.data.model.LedgerEntry
import com.patil.coconut_pfa.data.model.PaymentHistoryItem
import com.patil.coconut_pfa.data.model.PersonIOU
import com.patil.coconut_pfa.data.model.SmsReviewItem
import com.patil.coconut_pfa.data.model.SplitDetail
import com.patil.coconut_pfa.data.model.Subscription
import com.patil.coconut_pfa.data.model.Transaction
import com.patil.coconut_pfa.data.model.TransactionType
import com.patil.coconut_pfa.data.model.TrashItem
import kotlin.math.abs

object FakeData {

    // Initial account list with full type-specific panel details
    private val initialAccounts = listOf(
        Account(
            id = "acc_1",
            name = "Demo Bank Salary",
            type = AccountType.BANK,
            balanceInPaise = 24500000L, // ₹2,45,000
            accountNumberLast4 = "4321",
            bankName = "Demo Bank",
            includeInNetWorth = true
        ),
        Account(
            id = "acc_2",
            name = "Demo Bank Savings",
            type = AccountType.BANK,
            balanceInPaise = 15000000L, // ₹1,50,000
            accountNumberLast4 = "8765",
            bankName = "Demo Bank",
            includeInNetWorth = true
        ),
        Account(
            id = "acc_3",
            name = "Card A Premier",
            type = AccountType.CREDIT_CARD,
            balanceInPaise = -1850050L, // -₹18,500.50 outstanding
            unbilledAmountInPaise = 1250000L, // ₹12,500 unbilled
            dueAmountInPaise = 600050L, // ₹6,000.50 due statement amount
            minimumDueInPaise = 100000L, // ₹1,000 minimum due
            creditLimitInPaise = 15000000L, // ₹1,50,000 limit
            statementDay = 15,
            cardStatus = CardPaymentStatus.UNPAID,
            dueDate = "18 Nov 2024",
            accountNumberLast4 = "1122",
            bankName = "Demo Bank",
            includeInNetWorth = true
        ),
        Account(
            id = "acc_4",
            name = "Card B Rewards",
            type = AccountType.CREDIT_CARD,
            balanceInPaise = -840000L, // -₹8,400 outstanding
            unbilledAmountInPaise = 840000L, // ₹8,400 unbilled
            dueAmountInPaise = 0L,
            minimumDueInPaise = 0L,
            creditLimitInPaise = 10000000L, // ₹1,00,000 limit
            statementDay = 25,
            cardStatus = CardPaymentStatus.PAID,
            dueDate = "25 Nov 2024",
            accountNumberLast4 = "9988",
            bankName = "Other Bank",
            includeInNetWorth = true
        ),
        Account(
            id = "acc_5",
            name = "Cash Wallet",
            type = AccountType.CASH,
            balanceInPaise = 450000L, // ₹4,500
            includeInNetWorth = true
        ),
        Account(
            id = "acc_6",
            name = "Digital Wallet A",
            type = AccountType.WALLET,
            balanceInPaise = 125000L, // ₹1,250
            includeInNetWorth = true
        ),
        Account(
            id = "acc_7",
            name = "Mutual Funds Portfolio",
            type = AccountType.INVESTMENT,
            balanceInPaise = 52000000L, // ₹5,20,000
            currentValueInPaise = 52000000L,
            includeInNetWorth = true
        ),
        Account(
            id = "acc_8",
            name = "Fixed Deposit 1yr",
            type = AccountType.FIXED_DEPOSIT,
            balanceInPaise = 20000000L, // ₹2,00,000
            principalInPaise = 20000000L,
            fdInterestRate = 7.1,
            fdStartDate = "15 Jan 2024",
            maturityDate = "15 Jan 2025",
            maturityAmountInPaise = 21420000L, // ₹2,14,200
            includeInNetWorth = true
        ),
        Account(
            id = "acc_9",
            name = "Personal Loan Demo",
            type = AccountType.LOAN,
            balanceInPaise = -15000000L, // -₹1,50,000 loan balance
            dueAmountInPaise = 1500000L, // ₹15,000 EMI due
            emiAmountInPaise = 1500000L,
            interestRate = 10.5,
            monthsLeft = 11,
            dueDate = "05 Nov 2024",
            bankName = "Demo Bank",
            emiSchedule = listOf(
                EmiScheduleItem(1, "05 Nov 2024", 1500000L, 1368750L, 131250L, false),
                EmiScheduleItem(2, "05 Dec 2024", 1500000L, 1380720L, 119280L, false),
                EmiScheduleItem(3, "05 Jan 2025", 1500000L, 1392800L, 107200L, false),
                EmiScheduleItem(4, "05 Feb 2025", 1500000L, 1404980L, 95020L, false)
            ),
            includeInNetWorth = true
        )
    )

    val accounts = mutableStateListOf<Account>().apply { addAll(initialAccounts) }

    fun addAccount(account: Account) {
        accounts.add(account)
    }

    fun updateAccountBalance(accountId: String, newBalanceInPaise: Long) {
        val index = accounts.indexOfFirst { it.id == accountId }
        if (index != -1) {
            val oldAcc = accounts[index]
            accounts[index] = oldAcc.copy(
                balanceInPaise = newBalanceInPaise,
                currentValueInPaise = if (oldAcc.type == AccountType.INVESTMENT) newBalanceInPaise else oldAcc.currentValueInPaise
            )
        }
    }

    val netWorthInPaise: Long
        get() = accounts.filter { it.includeInNetWorth }.sumOf { it.balanceInPaise }

    val totalAssetsInPaise: Long
        get() = accounts.filter { it.includeInNetWorth && it.balanceInPaise > 0 }.sumOf { it.balanceInPaise }

    val totalLiabilitiesInPaise: Long
        get() = accounts.filter { it.includeInNetWorth && it.balanceInPaise < 0 }.sumOf { abs(it.balanceInPaise) }

    val sparklineTrendPoints: List<Float> = listOf(820f, 840f, 835f, 860f, 855f, 885f, 915f)

    val unbilledCreditCards: List<Account>
        get() = accounts.filter { it.type == AccountType.CREDIT_CARD && it.unbilledAmountInPaise > 0 }

    // Summary data for Payments Screen
    val dueIn7DaysInPaise: Long = 944950L // ₹9,449.50 (6000.50 CC + 2450 Utility + 999 Wifi)
    val overdueInPaise: Long = 1500000L   // ₹15,000 (EMI)
    val peopleOweYouInPaise: Long = 425000L // ₹4,250
    val youOwePeopleInPaise: Long = 120000L // ₹1,200

    private val initialDueItems = listOf(
        DueItem(
            id = "due_1",
            title = "Personal Loan EMI",
            amountInPaise = 1500000L, // ₹15,000
            dueDate = "05 Nov 2024",
            isOverdue = true,
            timeframe = DueTimeframe.OVERDUE,
            kind = BillKind.EMI,
            category = "EMI",
            accountName = "Personal Loan Demo"
        ),
        DueItem(
            id = "due_2",
            title = "Card A Premier Bill",
            amountInPaise = 600050L, // ₹6,000.50
            dueDate = "18 Nov 2024",
            isOverdue = false,
            timeframe = DueTimeframe.THIS_WEEK,
            kind = BillKind.OTHER,
            category = "Credit Card",
            accountName = "Card A Premier"
        ),
        DueItem(
            id = "due_3",
            title = "Electricity Bill",
            amountInPaise = 245000L, // ₹2,450
            dueDate = "22 Nov 2024",
            isOverdue = false,
            timeframe = DueTimeframe.THIS_WEEK,
            kind = BillKind.UTILITY,
            category = "Utilities"
        ),
        DueItem(
            id = "due_4",
            title = "Wifi Broadband",
            amountInPaise = 99900L, // ₹999
            dueDate = "24 Nov 2024",
            isOverdue = false,
            timeframe = DueTimeframe.THIS_WEEK,
            kind = BillKind.UTILITY,
            category = "Utilities"
        ),
        DueItem(
            id = "due_5",
            title = "House Rent",
            amountInPaise = 2200000L, // ₹22,000
            dueDate = "01 Dec 2024",
            isOverdue = false,
            timeframe = DueTimeframe.LATER,
            kind = BillKind.RENT,
            category = "Housing"
        ),
        DueItem(
            id = "due_6",
            title = "Car Insurance Premium",
            amountInPaise = 1450000L, // ₹14,500
            dueDate = "10 Dec 2024",
            isOverdue = false,
            timeframe = DueTimeframe.LATER,
            kind = BillKind.INSURANCE,
            category = "Insurance"
        )
    )

    val dueItems = mutableStateListOf<DueItem>().apply { addAll(initialDueItems) }

    fun addDueItem(item: DueItem) {
        dueItems.add(item)
    }

    val creditCardAccounts: List<Account>
        get() = accounts.filter { it.type == AccountType.CREDIT_CARD }

    val paymentHistory: List<PaymentHistoryItem> = listOf(
        PaymentHistoryItem(
            id = "pay_hist_1",
            title = "Card A Premier Bill Payment",
            amountInPaise = 600000L,
            paymentDate = "10 Nov 2024",
            kind = BillKind.OTHER,
            accountName = "Demo Bank Salary",
            status = "Paid"
        ),
        PaymentHistoryItem(
            id = "pay_hist_2",
            title = "Personal Loan EMI Oct",
            amountInPaise = 1500000L,
            paymentDate = "05 Oct 2024",
            kind = BillKind.EMI,
            accountName = "Demo Bank Salary",
            status = "Paid"
        ),
        PaymentHistoryItem(
            id = "pay_hist_3",
            title = "Electricity Bill Oct",
            amountInPaise = 210000L,
            paymentDate = "20 Oct 2024",
            kind = BillKind.UTILITY,
            accountName = "Demo Bank Savings",
            status = "Paid"
        ),
        PaymentHistoryItem(
            id = "pay_hist_4",
            title = "House Rent Oct",
            amountInPaise = 2200000L,
            paymentDate = "01 Oct 2024",
            kind = BillKind.RENT,
            accountName = "Demo Bank Salary",
            status = "Paid"
        )
    )

    val currentCycleSummary = CycleSummary(
        startDate = "25 Oct",
        endDate = "24 Nov",
        receivedInPaise = 12000000L, // ₹1,20,000
        spentInPaise = 4525050L     // ₹45,250.50
    )

    val smsReviewItems: List<SmsReviewItem> = listOf(
        SmsReviewItem(
            id = "sms_1",
            sender = "VM-DEMOBK",
            text = "Rs. 450.00 spent on Card A Premier at Coffee Shop 1 on 10 Nov. Avail Bal: Rs. 2,45,000.00",
            amountInPaise = 45000L, // ₹450
            accountName = "Card A Premier ••1122",
            timestamp = 1731234000000L,
            detectedCategory = "Dining",
            type = TransactionType.EXPENSE
        ),
        SmsReviewItem(
            id = "sms_2",
            sender = "JK-DEMOBK",
            text = "Rs 12500.00 credited to a/c XX4321 towards Salary adjustment. Avail Bal: Rs. 2,45,000.00",
            amountInPaise = 1250000L, // ₹12,500
            accountName = "Demo Bank Salary ••4321",
            timestamp = 1731147600000L,
            detectedCategory = "Income",
            type = TransactionType.INCOME
        ),
        SmsReviewItem(
            id = "sms_3",
            sender = "VM-DEMOBK",
            text = "Debited Rs. 2499.00 from a/c XX4321 for Electronics Store 1. Bank says balance Rs. 2,42,501.00",
            amountInPaise = 249900L, // ₹2,499
            accountName = "Demo Bank Salary ••4321",
            timestamp = 1731061200000L,
            detectedCategory = "Shopping",
            type = TransactionType.EXPENSE
        ),
        SmsReviewItem(
            id = "sms_4",
            sender = "BP-WALLETA",
            text = "Rs 350.50 paid to Grocery Merchant A via Wallet. Bal Rs. 1,250.00",
            amountInPaise = 35050L, // ₹350.50
            accountName = "Digital Wallet A",
            timestamp = 1730974800000L,
            detectedCategory = "Groceries",
            type = TransactionType.EXPENSE
        ),
        SmsReviewItem(
            id = "sms_5",
            sender = "VM-OTHERBK",
            text = "Alert: Rs 1200.00 spent on Card B Rewards at Fuel Station 1",
            amountInPaise = 120000L, // ₹1,200
            accountName = "Card B Rewards ••9988",
            timestamp = 1730888400000L,
            detectedCategory = "Fuel",
            type = TransactionType.EXPENSE
        ),
        SmsReviewItem(
            id = "sms_unparsed",
            sender = "AD-UNKBNK",
            text = "Txn alert: payment ref 9981245 processed successfully. Tap to review details.",
            amountInPaise = 0L,
            accountName = "Unknown Account",
            timestamp = 1730802000000L,
            detectedCategory = null,
            type = TransactionType.EXPENSE
        )
    )

    val transactions: List<Transaction> = listOf(
        Transaction(
            id = "tx_1",
            amountInPaise = 345000L, // ₹3,450
            type = TransactionType.EXPENSE,
            category = "Groceries",
            accountId = "acc_1",
            payee = "Supermarket A",
            timestamp = 1731408000000L, // 12 Nov 2024
            note = "Weekly grocery restock"
        ),
        Transaction(
            id = "tx_2",
            amountInPaise = 150000L, // ₹1,500
            type = TransactionType.TRANSFER,
            category = "Transfer",
            accountId = "acc_1",
            targetAccountId = "acc_5",
            payee = "Cash Withdrawal",
            timestamp = 1731393600000L, // 12 Nov 2024
            note = "Atm cash"
        ),
        Transaction(
            id = "tx_3",
            amountInPaise = 12000000L, // ₹1,20,000
            type = TransactionType.INCOME,
            category = "Salary",
            accountId = "acc_1",
            payee = "Employer Corp",
            timestamp = 1731321600000L, // 11 Nov 2024
            note = "November Salary"
        ),
        Transaction(
            id = "tx_4",
            amountInPaise = 85000L, // ₹850
            type = TransactionType.EXPENSE,
            category = "Dining",
            accountId = "acc_3",
            payee = "Restaurant B",
            timestamp = 1731307200000L, // 11 Nov 2024
            isSplit = true,
            splits = listOf(
                SplitDetail(category = "Dining", amountInPaise = 42500L, note = "My share"),
                SplitDetail(category = "Dining", amountInPaise = 42500L, note = "Friend 1 share")
            )
        ),
        Transaction(
            id = "tx_5",
            amountInPaise = 50000L, // ₹500
            type = TransactionType.REFUND,
            category = "Shopping",
            accountId = "acc_3",
            payee = "Online Store C",
            timestamp = 1731235200000L, // 10 Nov 2024
            note = "Returned item refund"
        ),
        Transaction(
            id = "tx_6",
            amountInPaise = 600000L, // ₹6,000
            type = TransactionType.SETTLEMENT,
            category = "Credit Card Payment",
            accountId = "acc_1",
            targetAccountId = "acc_3",
            payee = "Card A Settlement",
            timestamp = 1731220800000L, // 10 Nov 2024
            note = "Monthly CC repayment"
        )
    )

    val transactionsByDay: Map<String, List<Transaction>> = mapOf(
        "12 Nov 2024" to listOf(transactions[0], transactions[1]),
        "11 Nov 2024" to listOf(transactions[2], transactions[3]),
        "10 Nov 2024" to listOf(transactions[4], transactions[5])
    )

    // Goals Fake Data
    val initialGoals = listOf(
        Goal(
            id = "goal_1",
            title = "Emergency Fund",
            targetAmountInPaise = 50000000L, // ₹5,00,000
            currentAmountInPaise = 28000000L, // ₹2,80,000
            targetDate = "31 Dec 2025",
            perCycleNeededInPaise = 1500000L, // ₹15,000
            category = "Safety Net",
            contributions = listOf(
                GoalContribution("c_1", 2000000L, "15 Oct 2024", "Demo Bank Salary", "Monthly auto deposit"),
                GoalContribution("c_2", 1500000L, "15 Sep 2024", "Demo Bank Salary", "Monthly auto deposit"),
                GoalContribution("c_3", 5000000L, "01 Aug 2024", "Demo Bank Savings", "Bonus allocation")
            )
        ),
        Goal(
            id = "goal_2",
            title = "Japan Vacation 2025",
            targetAmountInPaise = 25000000L, // ₹2,50,000
            currentAmountInPaise = 14000000L, // ₹1,40,000
            targetDate = "31 May 2025",
            perCycleNeededInPaise = 1800000L, // ₹18,000
            category = "Travel",
            contributions = listOf(
                GoalContribution("c_4", 2500000L, "01 Oct 2024", "Demo Bank Savings", "Flight savings deposit"),
                GoalContribution("c_5", 3000000L, "01 Aug 2024", "Demo Bank Salary", "Summer bonus allocation")
            )
        ),
        Goal(
            id = "goal_3",
            title = "MacBook Pro M3",
            targetAmountInPaise = 18000000L, // ₹1,80,000
            currentAmountInPaise = 12000000L, // ₹1,20,000
            targetDate = "31 Mar 2025",
            perCycleNeededInPaise = 1200000L, // ₹12,000
            category = "Gadgets",
            contributions = listOf(
                GoalContribution("c_6", 1500000L, "10 Nov 2024", "Demo Bank Salary", "Tech upgrade fund")
            )
        )
    )

    val goals = mutableStateListOf<Goal>().apply { addAll(initialGoals) }

    fun addGoal(goal: Goal) {
        goals.add(goal)
    }

    fun addContributionToGoal(goalId: String, contribution: GoalContribution) {
        val index = goals.indexOfFirst { it.id == goalId }
        if (index != -1) {
            val oldGoal = goals[index]
            val updatedContribs = oldGoal.contributions + contribution
            goals[index] = oldGoal.copy(
                currentAmountInPaise = oldGoal.currentAmountInPaise + contribution.amountInPaise,
                contributions = updatedContribs
            )
        }
    }

    // Subscriptions Fake Data
    val initialSubscriptions = listOf(
        Subscription(
            id = "sub_1",
            name = "Netflix 4K Premium",
            priceInPaise = 64900L, // ₹649
            billingFrequency = BillingFrequency.MONTHLY,
            nextRenewalDate = "22 Nov 2024",
            category = "OTT & Streaming",
            paymentAccountName = "Card A Premier"
        ),
        Subscription(
            id = "sub_2",
            name = "Amazon Prime Annual",
            priceInPaise = 149900L, // ₹1,499
            billingFrequency = BillingFrequency.YEARLY,
            nextRenewalDate = "15 Jan 2025",
            category = "Shopping & OTT",
            paymentAccountName = "Demo Bank Salary"
        ),
        Subscription(
            id = "sub_3",
            name = "Spotify Family Plan",
            priceInPaise = 17900L, // ₹179
            billingFrequency = BillingFrequency.MONTHLY,
            nextRenewalDate = "28 Nov 2024",
            category = "Music",
            paymentAccountName = "Card B Rewards"
        ),
        Subscription(
            id = "sub_4",
            name = "JioFiber 300Mbps",
            priceInPaise = 117900L, // ₹1,179
            billingFrequency = BillingFrequency.MONTHLY,
            nextRenewalDate = "01 Dec 2024",
            category = "Broadband",
            paymentAccountName = "Demo Bank Salary"
        ),
        Subscription(
            id = "sub_5",
            name = "ChatGPT Plus AI",
            priceInPaise = 199900L, // ₹1,999
            billingFrequency = BillingFrequency.MONTHLY,
            nextRenewalDate = "05 Dec 2024",
            category = "Software & Tools",
            paymentAccountName = "Card A Premier"
        )
    )

    val subscriptions = mutableStateListOf<Subscription>().apply { addAll(initialSubscriptions) }

    val totalMonthlySubscriptionCostInPaise: Long
        get() = subscriptions.filter { it.isActive }.sumOf { it.monthlyEquivalentInPaise }

    fun addSubscription(sub: Subscription) {
        subscriptions.add(sub)
    }

    // Split & IOUs Fake Data
    val initialPersonIOUs = listOf(
        PersonIOU(
            id = "p_1",
            name = "Rahul Sharma",
            netBalanceInPaise = 350000L, // owes you ₹3,500
            phone = "+91 98765 43210",
            avatarColorHex = 0xFF4CAF50,
            ledger = listOf(
                LedgerEntry("l_1", "Goa Dinner Party", 500000L, "05 Nov 2024", paidByMe = true, myShareInPaise = 250000L, personShareInPaise = 250000L, note = "Split 50/50"),
                LedgerEntry("l_2", "Airport Cab Uber XL", 200000L, "02 Nov 2024", paidByMe = true, myShareInPaise = 100000L, personShareInPaise = 100000L, note = "Shared ride")
            )
        ),
        PersonIOU(
            id = "p_2",
            name = "Priya Patel",
            netBalanceInPaise = -120000L, // you owe ₹1,200
            phone = "+91 98123 45678",
            avatarColorHex = 0xFFE53935,
            ledger = listOf(
                LedgerEntry("l_3", "Concert Tickets", 360000L, "28 Oct 2024", paidByMe = false, myShareInPaise = 180000L, personShareInPaise = 180000L, note = "Priya bought tickets"),
                LedgerEntry("l_4", "Starbucks Coffee & Snacks", 120000L, "01 Nov 2024", paidByMe = true, myShareInPaise = 60000L, personShareInPaise = 60000L, note = "Me paid")
            )
        ),
        PersonIOU(
            id = "p_3",
            name = "Amit Kumar",
            netBalanceInPaise = 75000L, // owes you ₹750
            phone = "+91 97654 32109",
            avatarColorHex = 0xFF4CAF50,
            ledger = listOf(
                LedgerEntry("l_5", "Office Lunch Zomato", 150000L, "10 Nov 2024", paidByMe = true, myShareInPaise = 75000L, personShareInPaise = 75000L, note = "Shared lunch")
            )
        )
    )

    val personIOUs = mutableStateListOf<PersonIOU>().apply { addAll(initialPersonIOUs) }

    fun addSplitTransaction(
        title: String,
        totalAmountInPaise: Long,
        personName: String,
        paidByMe: Boolean,
        personShareInPaise: Long
    ) {
        val index = personIOUs.indexOfFirst { it.name.equals(personName, ignoreCase = true) }
        val myShare = totalAmountInPaise - personShareInPaise
        val entry = LedgerEntry(
            id = "l_${System.currentTimeMillis()}",
            title = title,
            amountInPaise = totalAmountInPaise,
            date = "Today",
            paidByMe = paidByMe,
            myShareInPaise = myShare,
            personShareInPaise = personShareInPaise
        )

        val balanceDelta = if (paidByMe) personShareInPaise else -myShare

        if (index != -1) {
            val oldPerson = personIOUs[index]
            personIOUs[index] = oldPerson.copy(
                netBalanceInPaise = oldPerson.netBalanceInPaise + balanceDelta,
                ledger = listOf(entry) + oldPerson.ledger
            )
        } else {
            personIOUs.add(
                PersonIOU(
                    id = "p_${System.currentTimeMillis()}",
                    name = personName,
                    netBalanceInPaise = balanceDelta,
                    ledger = listOf(entry)
                )
            )
        }
    }

    fun settleUpPerson(personId: String, settlementAmountInPaise: Long) {
        val index = personIOUs.indexOfFirst { it.id == personId }
        if (index != -1) {
            val oldPerson = personIOUs[index]
            val newBal = if (oldPerson.netBalanceInPaise > 0) {
                (oldPerson.netBalanceInPaise - settlementAmountInPaise).coerceAtLeast(0L)
            } else {
                (oldPerson.netBalanceInPaise + settlementAmountInPaise).coerceAtMost(0L)
            }
            val settlementEntry = LedgerEntry(
                id = "settle_${System.currentTimeMillis()}",
                title = "Settle Up Payment",
                amountInPaise = settlementAmountInPaise,
                date = "Today",
                paidByMe = oldPerson.netBalanceInPaise < 0,
                myShareInPaise = settlementAmountInPaise,
                personShareInPaise = 0L,
                note = "Settled balance"
            )
            personIOUs[index] = oldPerson.copy(
                netBalanceInPaise = newBal,
                ledger = listOf(settlementEntry) + oldPerson.ledger
            )
        }
    }

    // Reports Fake Data
    val reportCycles = listOf(
        "25 Oct – 24 Nov 2024 (Current)",
        "25 Sep – 24 Oct 2024",
        "25 Aug – 24 Sep 2024",
        "25 Jul – 24 Aug 2024"
    )

    val categorySpendData = listOf(
        CategorySpendItem("Housing & Rent", 2200000L, 48.6f, Color(0xFF2E7D32)),
        CategorySpendItem("Dining & Food", 845000L, 18.7f, Color(0xFFFB8C00)),
        CategorySpendItem("Shopping & Electronics", 650000L, 14.3f, Color(0xFF0288D1)),
        CategorySpendItem("Groceries", 520000L, 11.5f, Color(0xFF8E24AA)),
        CategorySpendItem("Utilities & Wifi", 310050L, 6.9f, Color(0xFFD81B60))
    )

    val cycleSpendHistory6 = listOf(
        CycleSpendItem("Jun 2024", 11500000L, 4100000L),
        CycleSpendItem("Jul 2024", 12000000L, 4600000L),
        CycleSpendItem("Aug 2024", 12000000L, 4300000L),
        CycleSpendItem("Sep 2024", 12500000L, 4900000L),
        CycleSpendItem("Oct 2024", 12000000L, 4200000L),
        CycleSpendItem("Nov 2024", 12000000L, 4525050L)
    )

    val cycleSpendHistory12 = listOf(
        CycleSpendItem("Dec 23", 11000000L, 4800000L),
        CycleSpendItem("Jan 24", 11500000L, 4300000L),
        CycleSpendItem("Feb 24", 11500000L, 3900000L),
        CycleSpendItem("Mar 24", 13000000L, 5200000L),
        CycleSpendItem("Apr 24", 11500000L, 4100000L),
        CycleSpendItem("May 24", 12000000L, 4400000L),
        CycleSpendItem("Jun 24", 11500000L, 4100000L),
        CycleSpendItem("Jul 24", 12000000L, 4600000L),
        CycleSpendItem("Aug 24", 12000000L, 4300000L),
        CycleSpendItem("Sep 24", 12500000L, 4900000L),
        CycleSpendItem("Oct 24", 12000000L, 4200000L),
        CycleSpendItem("Nov 24", 12000000L, 4525050L)
    )

    val dailySpendHeatmap = (1..30).map { day ->
        val amount = when {
            day == 1 || day == 15 -> 2200000L // Rent / Large bill
            day % 7 == 0 -> 850000L // Weekend outing
            day % 3 == 0 -> 350000L
            else -> (50000L..150000L).random()
        }
        val intensity = (amount.toFloat() / 2200000f).coerceIn(0.1f, 1.0f)
        DailySpendHeatmapItem(
            dayOfMonth = day,
            dateLabel = "$day Nov",
            amountInPaise = amount,
            intensity = intensity
        )
    }

    // Trash Fake Data
    val initialTrashItems = listOf(
        TrashItem("t_1", "Supermarket Grocery Purchase", "Transaction", 345000L, "10 Nov 2024", 28),
        TrashItem("t_2", "Old Cash Wallet", "Account", 0L, "26 Oct 2024", 14),
        TrashItem("t_3", "Gym Subscription Renewal", "Bill", 250000L, "18 Oct 2024", 6)
    )

    val trashItems = mutableStateListOf<TrashItem>().apply { addAll(initialTrashItems) }

    fun restoreTrashItem(item: TrashItem) {
        trashItems.remove(item)
    }

    fun deleteTrashItemPermanently(item: TrashItem) {
        trashItems.remove(item)
    }
}

